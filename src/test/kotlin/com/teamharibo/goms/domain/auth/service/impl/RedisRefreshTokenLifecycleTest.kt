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
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.longs.shouldBeInRange
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import java.util.Optional
import java.util.concurrent.TimeUnit

private const val MEMBER_ID = 1L
private const val REDIS_KEY = "auth:refresh:$MEMBER_ID"
private const val SECRET = "refresh-token-lifecycle-test-secret-key-1234567890"
private const val OTHER_SECRET = "refresh-token-lifecycle-other-secret-key-0987654321"
private const val ACCESS_EXP_SECONDS = 300L
private const val REFRESH_EXP_SECONDS = 600L

// 재발급 후 TTL이 새로 설정됐는지 구분하기 위해 로그인 시 저장 TTL을 재발급 TTL보다 짧게 둔다.
private const val SIGNIN_TTL_SECONDS = 60L

/**
 * 실제 JwtProvider 서명·만료 검증과 실제 Redis 저장소로 Refresh Token 수명주기(#89)를 순차 검증한다.
 *
 * 재발급·로그아웃 경합은 RedisRefreshTokenConcurrencyTest(#85)가 다루므로 여기서는 다루지 않는다.
 */
@EnabledIf(DockerAvailableCondition::class)
class RedisRefreshTokenLifecycleTest : DescribeSpec({

    val redis = RefreshTokenRedisContainer()
    val jwtProvider = JwtProvider(
        JwtProperties(
            secret = SECRET,
            accessExpSeconds = ACCESS_EXP_SECONDS,
            refreshExpSeconds = REFRESH_EXP_SECONDS
        )
    )

    lateinit var connectionFactory: LettuceConnectionFactory
    lateinit var redisTemplate: StringRedisTemplate
    lateinit var refreshTokenRepository: RefreshTokenRedisRepository
    lateinit var reissueService: ReissueServiceImpl
    lateinit var signoutService: SignoutServiceImpl

    // 로그인 성공 상태를 재현한다: 발급한 Refresh Token을 TTL과 함께 Redis에 저장한다.
    fun signin(): String =
        jwtProvider.createRefreshToken(MEMBER_ID).also {
            refreshTokenRepository.save(MEMBER_ID, it, SIGNIN_TTL_SECONDS)
        }

    fun ttlSeconds(): Long = redisTemplate.getExpire(REDIS_KEY, TimeUnit.SECONDS)

    fun reissueErrorCode(token: String): ErrorCode =
        shouldThrow<GlobalException> { reissueService.reissue("Bearer $token") }.errorCode

    beforeSpec { redis.start() }

    afterSpec { redis.stop() }

    beforeEach {
        connectionFactory = redis.connectionFactory()
        redisTemplate = StringRedisTemplate(connectionFactory)
        redisTemplate.execute { connection ->
            connection.serverCommands().flushAll()
            null
        }

        val member = mockk<Member>().also {
            every { it.id } returns MEMBER_ID
            every { it.role } returns Role.ROLE_STUDENT
        }
        val memberRepository = mockk<MemberRepository>()
        every { memberRepository.findById(MEMBER_ID) } returns Optional.of(member)

        refreshTokenRepository = RefreshTokenRedisRepository(redisTemplate)
        reissueService = ReissueServiceImpl(refreshTokenRepository, memberRepository, jwtProvider)
        signoutService = SignoutServiceImpl(refreshTokenRepository, jwtProvider)
    }

    afterEach {
        connectionFactory.destroy()
    }

    describe("Refresh Token 수명주기") {

        context("Given: 로그인으로 저장된 유효한 Refresh Token") {
            it("When: 재발급하면 Then: 새 토큰을 반환하고 저장 토큰을 교체하며 TTL을 재설정한다") {
                val signinToken = signin()

                val response = reissueService.reissue("Bearer $signinToken")

                response.refreshToken shouldNotBe signinToken
                jwtProvider.parseClaims(response.accessToken).let {
                    it["type"] shouldBe "ACCESS"
                    it["role"] shouldBe Role.ROLE_STUDENT.name
                    it.subject shouldBe MEMBER_ID.toString()
                }
                jwtProvider.parseClaims(response.refreshToken).let {
                    it["type"] shouldBe "REFRESH"
                    it.subject shouldBe MEMBER_ID.toString()
                }
                refreshTokenRepository.findByMemberId(MEMBER_ID) shouldBe response.refreshToken
                ttlSeconds() shouldBeInRange (SIGNIN_TTL_SECONDS + 1)..REFRESH_EXP_SECONDS
            }
        }

        context("Given: 재발급으로 교체된 이전 Refresh Token") {
            it("When: 이전 토큰으로 다시 재발급하면 Then: INVALID_REFRESH_TOKEN으로 거부하고 새 토큰을 유지한다") {
                val oldToken = signin()
                val rotated = reissueService.reissue("Bearer $oldToken")

                reissueErrorCode(oldToken) shouldBe ErrorCode.INVALID_REFRESH_TOKEN
                refreshTokenRepository.findByMemberId(MEMBER_ID) shouldBe rotated.refreshToken
            }
        }

        context("Given: 로그아웃으로 폐기된 Refresh Token") {
            it("When: 같은 토큰으로 재발급하면 Then: INVALID_REFRESH_TOKEN으로 거부하고 Redis 키가 남지 않는다") {
                val token = signin()
                signoutService.signout("Bearer $token")

                redisTemplate.hasKey(REDIS_KEY) shouldBe false
                reissueErrorCode(token) shouldBe ErrorCode.INVALID_REFRESH_TOKEN
                refreshTokenRepository.findByMemberId(MEMBER_ID).shouldBeNull()
            }
        }

        context("Given: Refresh Token 대신 전달된 Access Token") {
            it("When: 재발급하면 Then: INVALID_REFRESH_TOKEN으로 거부하고 저장 토큰을 유지한다") {
                val storedToken = signin()
                val accessToken = jwtProvider.createAccessToken(MEMBER_ID, Role.ROLE_STUDENT.name)

                reissueErrorCode(accessToken) shouldBe ErrorCode.INVALID_REFRESH_TOKEN
                refreshTokenRepository.findByMemberId(MEMBER_ID) shouldBe storedToken
            }
        }

        context("Given: 만료 시각이 지난 Refresh Token") {
            it("When: 재발급하면 Then: 실제 만료 검증으로 INVALID_REFRESH_TOKEN을 반환하고 Redis를 변경하지 않는다") {
                val storedToken = signin()
                val expiredToken = JwtProvider(
                    JwtProperties(
                        secret = SECRET,
                        accessExpSeconds = ACCESS_EXP_SECONDS,
                        refreshExpSeconds = -60L
                    )
                ).createRefreshToken(MEMBER_ID)

                reissueErrorCode(expiredToken) shouldBe ErrorCode.INVALID_REFRESH_TOKEN
                refreshTokenRepository.findByMemberId(MEMBER_ID) shouldBe storedToken
                ttlSeconds() shouldBeInRange 1L..SIGNIN_TTL_SECONDS
            }
        }

        context("Given: 다른 키로 서명된 Refresh Token") {
            it("When: 재발급하면 Then: 서명 검증으로 INVALID_REFRESH_TOKEN을 반환하고 Redis를 변경하지 않는다") {
                val storedToken = signin()
                val forgedToken = JwtProvider(
                    JwtProperties(
                        secret = OTHER_SECRET,
                        accessExpSeconds = ACCESS_EXP_SECONDS,
                        refreshExpSeconds = REFRESH_EXP_SECONDS
                    )
                ).createRefreshToken(MEMBER_ID)

                reissueErrorCode(forgedToken) shouldBe ErrorCode.INVALID_REFRESH_TOKEN
                refreshTokenRepository.findByMemberId(MEMBER_ID) shouldBe storedToken
            }
        }
    }
})
