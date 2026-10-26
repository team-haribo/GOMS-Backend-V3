package com.teamharibo.goms.domain.auth.service.impl

import com.teamharibo.goms.domain.auth.repository.redis.DockerAvailableCondition
import com.teamharibo.goms.domain.auth.repository.redis.RefreshTokenRedisContainer
import com.teamharibo.goms.domain.auth.repository.redis.RefreshTokenRedisRepository
import com.teamharibo.goms.domain.common.enums.Role
import com.teamharibo.goms.domain.member.entity.Member
import com.teamharibo.goms.domain.member.repository.MemberRepository
import com.teamharibo.goms.global.exception.ErrorCode
import com.teamharibo.goms.global.exception.GlobalException
import com.teamharibo.goms.global.jwt.JwtProperties
import com.teamharibo.goms.global.jwt.JwtProvider
import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import java.util.Optional
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

private const val MEMBER_ID = 1L
private const val TTL_SECONDS = 300L
private const val TIMEOUT_SECONDS = 5L

/**
 * Refresh Token 재발급·로그아웃 경합(#85)을 실제 Redis로 재현한다.
 *
 * 각 시나리오는 Redis 조회와 변경 사이에 다른 요청이 끼어드는 순서를 Gate로 고정한다.
 */
@EnabledIf(DockerAvailableCondition::class)
class RedisRefreshTokenConcurrencyTest : DescribeSpec({

    val redis = RefreshTokenRedisContainer()
    val jwtProvider = JwtProvider(
        JwtProperties(
            secret = "refresh-token-concurrency-test-secret-key-1234567890",
            accessExpSeconds = TTL_SECONDS,
            refreshExpSeconds = TTL_SECONDS
        )
    )
    val memberLookupGate = AtomicReference<Gate?>()

    lateinit var connectionFactory: LettuceConnectionFactory
    lateinit var refreshTokenRepository: GatedRefreshTokenRedisRepository
    lateinit var reissueService: ReissueServiceImpl
    lateinit var signoutService: SignoutServiceImpl
    lateinit var executor: ExecutorService

    fun saveCurrentRefreshToken(): String =
        jwtProvider.createRefreshToken(MEMBER_ID).also {
            refreshTokenRepository.save(MEMBER_ID, it, TTL_SECONDS)
        }

    fun <T> Result<T>.errorCode(): ErrorCode? =
        (exceptionOrNull() as? GlobalException)?.errorCode

    beforeSpec { redis.start() }

    afterSpec { redis.stop() }

    beforeEach {
        connectionFactory = redis.connectionFactory()
        val redisTemplate = StringRedisTemplate(connectionFactory)
        redisTemplate.execute { connection ->
            connection.serverCommands().flushAll()
            null
        }

        val member = mockk<Member>().also {
            every { it.id } returns MEMBER_ID
            every { it.role } returns Role.ROLE_STUDENT
        }
        val memberRepository = mockk<MemberRepository>()
        every { memberRepository.findById(MEMBER_ID) } answers {
            memberLookupGate.get()?.pass()
            Optional.of(member)
        }

        memberLookupGate.set(null)
        refreshTokenRepository = GatedRefreshTokenRedisRepository(redisTemplate)
        reissueService = ReissueServiceImpl(refreshTokenRepository, memberRepository, jwtProvider)
        signoutService = SignoutServiceImpl(refreshTokenRepository, jwtProvider)
        executor = Executors.newFixedThreadPool(2)
    }

    afterEach {
        memberLookupGate.get()?.release()
        refreshTokenRepository.findGate?.release()
        executor.shutdownNow()
        executor.awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS) shouldBe true
        connectionFactory.destroy()
    }

    describe("Refresh Token 재발급·로그아웃 경합") {

        context("Given: 재발급 요청이 토큰 검증을 마친 직후 로그아웃이 성공") {
            it("When: 재발급이 이어서 진행되면 Then: 세션을 다시 저장하지 않고 INVALID_REFRESH_TOKEN으로 거부한다") {
                val oldToken = saveCurrentRefreshToken()
                val gate = Gate(1).also { memberLookupGate.set(it) }

                // 재발급 요청: 저장된 토큰 검증을 마친 뒤 회원 조회에서 멈춘다.
                val reissue = executor.submit(Callable { runCatching { reissueService.reissue("Bearer $oldToken") } })
                gate.awaitArrival() shouldBe true

                signoutService.signout("Bearer $oldToken")
                gate.release()
                val reissueResult = reissue.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)

                refreshTokenRepository.findByMemberId(MEMBER_ID).shouldBeNull()
                reissueResult.errorCode() shouldBe ErrorCode.INVALID_REFRESH_TOKEN
            }
        }

        context("Given: 같은 Refresh Token으로 두 재발급 요청이 모두 토큰 검증을 통과") {
            it("When: 두 요청이 저장을 진행하면 Then: 한 요청만 성공하고 나머지는 INVALID_REFRESH_TOKEN으로 거부한다") {
                val oldToken = saveCurrentRefreshToken()
                val gate = Gate(2).also { memberLookupGate.set(it) }

                val reissues = List(2) {
                    executor.submit(Callable { runCatching { reissueService.reissue("Bearer $oldToken") } })
                }
                gate.awaitArrival() shouldBe true
                gate.release()
                val results = reissues.map { it.get(TIMEOUT_SECONDS, TimeUnit.SECONDS) }

                val successes = results.mapNotNull { it.getOrNull() }
                successes shouldHaveSize 1
                results.filter { it.isFailure }.map { it.errorCode() } shouldContainExactly
                    listOf(ErrorCode.INVALID_REFRESH_TOKEN)
                refreshTokenRepository.findByMemberId(MEMBER_ID) shouldBe successes.single().refreshToken
            }
        }

        context("Given: 로그아웃 요청이 저장된 토큰을 조회한 직후 재발급이 완료") {
            it("When: 로그아웃이 이어서 진행되면 Then: 새 토큰을 지우지 않고 INVALID_REFRESH_TOKEN으로 거부한다") {
                val oldToken = saveCurrentRefreshToken()
                val gate = Gate(1).also { refreshTokenRepository.findGate = it }

                // 로그아웃 요청: 저장된 토큰 조회 직후, 비교와 삭제 전에 멈춘다.
                val signout = executor.submit(Callable { runCatching { signoutService.signout("Bearer $oldToken") } })
                gate.awaitArrival() shouldBe true

                val reissued = reissueService.reissue("Bearer $oldToken")
                gate.release()
                val signoutResult = signout.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)

                refreshTokenRepository.findByMemberId(MEMBER_ID) shouldBe reissued.refreshToken
                signoutResult.errorCode() shouldBe ErrorCode.INVALID_REFRESH_TOKEN
            }
        }
    }
})

/**
 * 지정한 수의 호출을 멈춰 세웠다가 release 시 함께 진행시킨다. 이후 호출은 그대로 통과한다.
 */
private class Gate(parties: Int) {
    private val remaining = AtomicInteger(parties)
    private val arrived = CountDownLatch(parties)
    private val released = CountDownLatch(1)

    fun pass() {
        if (remaining.getAndDecrement() <= 0) return
        arrived.countDown()
        check(released.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) { "Gate가 제한 시간 안에 해제되지 않았다" }
    }

    fun awaitArrival(): Boolean = arrived.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)

    fun release() = released.countDown()
}

private class GatedRefreshTokenRedisRepository(
    redisTemplate: StringRedisTemplate
) : RefreshTokenRedisRepository(redisTemplate) {
    @Volatile
    var findGate: Gate? = null

    override fun findByMemberId(memberId: Long): String? =
        super.findByMemberId(memberId).also { findGate?.pass() }
}
