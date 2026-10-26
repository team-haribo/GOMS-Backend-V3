package com.teamharibo.goms.domain.auth.service.impl

import com.teamharibo.goms.domain.auth.repository.redis.RefreshTokenRedisRepository
import com.teamharibo.goms.fixture.AuthFixture
import com.teamharibo.goms.global.exception.ErrorCode
import com.teamharibo.goms.global.exception.GlobalException
import com.teamharibo.goms.global.jwt.JwtProvider
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify

class SignoutServiceImplTest : DescribeSpec({

    val refreshTokenRepository = mockk<RefreshTokenRedisRepository>()
    val jwtProvider = mockk<JwtProvider>()
    val service = SignoutServiceImpl(refreshTokenRepository, jwtProvider)

    val memberId = 1L

    fun validClaims(): Claims = mockk<Claims>().also { claims ->
        every { claims["type"] } returns "REFRESH"
        every { claims.subject } returns memberId.toString()
    }

    afterEach {
        clearAllMocks()
    }

    describe("SignoutService") {

        context("Given: 만료되어 파싱할 수 없는 리프레시 토큰") {
            it("When: 로그아웃 시 Then: 401 INVALID_REFRESH_TOKEN 예외가 발생하고 토큰을 삭제하지 않는다") {
                every { jwtProvider.parseClaims("expired-refresh-token") } throws
                    ExpiredJwtException(null, null, "expired")

                val exception = shouldThrow<GlobalException> {
                    service.signout("Bearer expired-refresh-token")
                }

                exception.errorCode shouldBe ErrorCode.INVALID_REFRESH_TOKEN
                exception.errorCode.status shouldBe 401
                verify(exactly = 0) { refreshTokenRepository.findByMemberId(any()) }
                verify(exactly = 0) { refreshTokenRepository.deleteByMemberId(any()) }
            }
        }

        context("Given: 서명 또는 형식이 잘못된 리프레시 토큰") {
            it("When: 로그아웃 시 Then: 401 INVALID_REFRESH_TOKEN 예외가 발생한다") {
                every { jwtProvider.parseClaims("malformed-refresh-token") } throws
                    MalformedJwtException("malformed")

                val exception = shouldThrow<GlobalException> {
                    service.signout("Bearer malformed-refresh-token")
                }

                exception.errorCode shouldBe ErrorCode.INVALID_REFRESH_TOKEN
                exception.errorCode.status shouldBe 401
                verify(exactly = 0) { refreshTokenRepository.deleteByMemberId(any()) }
            }
        }

        context("Given: 유효한 Bearer 리프레시 토큰") {
            it("When: 로그아웃 시 Then: Redis에서 리프레시 토큰을 삭제한다") {
                val claims = validClaims()

                every { jwtProvider.parseClaims(AuthFixture.VALID_REFRESH_TOKEN) } returns claims
                every { refreshTokenRepository.findByMemberId(memberId) } returns AuthFixture.VALID_REFRESH_TOKEN
                justRun { refreshTokenRepository.deleteByMemberId(memberId) }

                service.signout("Bearer ${AuthFixture.VALID_REFRESH_TOKEN}")

                verify(exactly = 1) { refreshTokenRepository.deleteByMemberId(memberId) }
            }
        }

        context("Given: 저장된 토큰이 없는 경우") {
            it("When: 로그아웃 시 Then: INVALID_REFRESH_TOKEN 예외가 발생한다") {
                val claims = validClaims()

                every { jwtProvider.parseClaims("unknown-token") } returns claims
                every { refreshTokenRepository.findByMemberId(memberId) } returns null

                shouldThrow<GlobalException> {
                    service.signout("Bearer unknown-token")
                }.errorCode shouldBe ErrorCode.INVALID_REFRESH_TOKEN
            }
        }

        context("Given: Bearer 접두사 없는 토큰") {
            it("When: 로그아웃 시 Then: Bearer 없이도 정상 처리된다") {
                val claims = validClaims()

                every { jwtProvider.parseClaims(AuthFixture.VALID_REFRESH_TOKEN) } returns claims
                every { refreshTokenRepository.findByMemberId(memberId) } returns AuthFixture.VALID_REFRESH_TOKEN
                justRun { refreshTokenRepository.deleteByMemberId(memberId) }

                service.signout(AuthFixture.VALID_REFRESH_TOKEN)

                verify(exactly = 1) { refreshTokenRepository.deleteByMemberId(memberId) }
            }
        }
    }
})