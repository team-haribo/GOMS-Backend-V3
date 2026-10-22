package com.teamharibo.goms.domain.auth.service.impl

import com.teamharibo.goms.domain.auth.repository.redis.RefreshTokenRedisContainer
import com.teamharibo.goms.domain.auth.repository.redis.RefreshTokenRedisRepository
import com.teamharibo.goms.domain.common.enums.Role
import com.teamharibo.goms.domain.member.entity.Member
import com.teamharibo.goms.domain.member.repository.MemberRepository
import com.teamharibo.goms.global.exception.ErrorCode
import com.teamharibo.goms.global.exception.GlobalException
import com.teamharibo.goms.global.jwt.JwtProperties
import com.teamharibo.goms.global.jwt.JwtProvider
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.Optional
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Refresh Token 재발급·로그아웃 경합(#85)을 실제 Redis로 재현한다.
 *
 * 각 시나리오는 Redis 조회와 변경 사이에 다른 요청이 끼어드는 순서를 Gate로 고정한다.
 */
@Testcontainers(disabledWithoutDocker = true)
class RedisRefreshTokenConcurrencyTest {

    companion object {
        private const val MEMBER_ID = 1L
        private const val TTL_SECONDS = 300L
        private const val TIMEOUT_SECONDS = 5L

        @Container
        @JvmField
        val redis: RefreshTokenRedisContainer = RefreshTokenRedisContainer().withExposedPorts(6379)
    }

    private lateinit var connectionFactory: LettuceConnectionFactory
    private lateinit var refreshTokenRepository: GatedRefreshTokenRedisRepository
    private lateinit var reissueService: ReissueServiceImpl
    private lateinit var signoutService: SignoutServiceImpl
    private lateinit var executor: ExecutorService

    private val jwtProvider = JwtProvider(
        JwtProperties(
            secret = "refresh-token-concurrency-test-secret-key-1234567890",
            accessExpSeconds = TTL_SECONDS,
            refreshExpSeconds = TTL_SECONDS
        )
    )

    @Volatile
    private var memberLookupGate: Gate? = null

    @BeforeEach
    fun setUp() {
        connectionFactory = LettuceConnectionFactory(redis.host, redis.getMappedPort(6379)).apply {
            afterPropertiesSet()
            start()
        }
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
            memberLookupGate?.pass()
            Optional.of(member)
        }

        refreshTokenRepository = GatedRefreshTokenRedisRepository(redisTemplate)
        reissueService = ReissueServiceImpl(refreshTokenRepository, memberRepository, jwtProvider)
        signoutService = SignoutServiceImpl(refreshTokenRepository, jwtProvider)
        executor = Executors.newFixedThreadPool(2)
    }

    @AfterEach
    fun tearDown() {
        memberLookupGate?.release()
        refreshTokenRepository.findGate?.release()
        executor.shutdownNow()
        assertTrue(executor.awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS), "작업 스레드가 종료되어야 한다")
        connectionFactory.destroy()
    }

    @Test
    fun `재발급 검증 직후 로그아웃이 성공하면 세션이 다시 저장되지 않는다`() {
        val oldToken = saveCurrentRefreshToken()
        val gate = Gate(1).also { memberLookupGate = it }

        // 재발급 요청: 저장된 토큰 검증을 마친 뒤 회원 조회에서 멈춘다.
        val reissue = executor.submit(Callable { runCatching { reissueService.reissue("Bearer $oldToken") } })
        assertTrue(gate.awaitArrival(), "재발급 요청이 토큰 검증을 마쳐야 한다")

        signoutService.signout("Bearer $oldToken")
        gate.release()
        val reissueResult = reissue.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)

        assertNull(refreshTokenRepository.findByMemberId(MEMBER_ID), "로그아웃 이후 Refresh Token이 다시 저장되면 안 된다")
        assertEquals(ErrorCode.INVALID_REFRESH_TOKEN, reissueResult.errorCode(), "늦게 끝난 재발급은 거부되어야 한다")
    }

    @Test
    fun `같은 Refresh Token으로 동시에 재발급하면 한 요청만 성공한다`() {
        val oldToken = saveCurrentRefreshToken()
        val gate = Gate(2).also { memberLookupGate = it }

        // 두 재발급 요청이 모두 같은 토큰 검증을 통과한 뒤 저장을 시작하게 한다.
        val reissues = List(2) {
            executor.submit(Callable { runCatching { reissueService.reissue("Bearer $oldToken") } })
        }
        assertTrue(gate.awaitArrival(), "두 재발급 요청이 모두 토큰 검증을 마쳐야 한다")
        gate.release()
        val results = reissues.map { it.get(TIMEOUT_SECONDS, TimeUnit.SECONDS) }

        val successes = results.mapNotNull { it.getOrNull() }
        assertEquals(1, successes.size, "같은 Refresh Token의 재발급은 한 번만 성공해야 한다")
        assertEquals(
            listOf(ErrorCode.INVALID_REFRESH_TOKEN),
            results.filter { it.isFailure }.map { it.errorCode() },
            "나머지 재발급은 INVALID_REFRESH_TOKEN으로 거부되어야 한다"
        )
        assertEquals(successes.single().refreshToken, refreshTokenRepository.findByMemberId(MEMBER_ID))
    }

    @Test
    fun `로그아웃 검증 직후 재발급된 새 토큰은 오래된 로그아웃 요청이 지우지 않는다`() {
        val oldToken = saveCurrentRefreshToken()
        val gate = Gate(1).also { refreshTokenRepository.findGate = it }

        // 로그아웃 요청: 저장된 토큰 조회 직후, 비교와 삭제 전에 멈춘다.
        val signout = executor.submit(Callable { runCatching { signoutService.signout("Bearer $oldToken") } })
        assertTrue(gate.awaitArrival(), "로그아웃 요청이 저장된 토큰을 조회해야 한다")

        val reissued = reissueService.reissue("Bearer $oldToken")
        gate.release()
        val signoutResult = signout.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)

        assertEquals(
            reissued.refreshToken,
            refreshTokenRepository.findByMemberId(MEMBER_ID),
            "새로 발급된 Refresh Token은 오래된 로그아웃 요청으로 삭제되면 안 된다"
        )
        assertEquals(ErrorCode.INVALID_REFRESH_TOKEN, signoutResult.errorCode(), "오래된 로그아웃은 거부되어야 한다")
    }

    private fun saveCurrentRefreshToken(): String =
        jwtProvider.createRefreshToken(MEMBER_ID).also {
            refreshTokenRepository.save(MEMBER_ID, it, TTL_SECONDS)
        }

    private fun <T> Result<T>.errorCode(): ErrorCode? =
        (exceptionOrNull() as? GlobalException)?.errorCode

    /**
     * 지정한 수의 호출을 멈춰 세웠다가 release 시 함께 진행시킨다. 이후 호출은 그대로 통과한다.
     */
    class Gate(parties: Int) {
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

    class GatedRefreshTokenRedisRepository(
        redisTemplate: StringRedisTemplate
    ) : RefreshTokenRedisRepository(redisTemplate) {
        @Volatile
        var findGate: Gate? = null

        override fun findByMemberId(memberId: Long): String? =
            super.findByMemberId(memberId).also { findGate?.pass() }
    }
}
