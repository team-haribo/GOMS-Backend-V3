package com.example.team.haribo.goms.domain.auth.service.impl

import com.example.team.haribo.goms.domain.auth.repository.redis.RefreshTokenRedisRepository
import com.example.team.haribo.goms.fixture.AuthFixture
import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException
import com.example.team.haribo.goms.global.jwt.JwtProvider
import io.jsonwebtoken.Claims
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
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

    describe("SignoutService") {

        context("Given: 유효한 Bearer 리프레시 토큰") {
            val claims = validClaims()
            every { jwtProvider.parseClaims(AuthFixture.VALID_REFRESH_TOKEN) } returns claims
            every { refreshTokenRepository.findByMemberId(memberId) } returns AuthFixture.VALID_REFRESH_TOKEN
            justRun { refreshTokenRepository.deleteByMemberId(memberId) }

            it("When: 로그아웃 시 Then: Redis에서 리프레시 토큰을 삭제한다") {
                service.signout("Bearer ${AuthFixture.VALID_REFRESH_TOKEN}")
                verify(exactly = 1) { refreshTokenRepository.deleteByMemberId(memberId) }
            }
        }

        context("Given: 저장된 토큰이 없는 경우") {
            val claims = validClaims()
            every { jwtProvider.parseClaims("unknown-token") } returns claims
            every { refreshTokenRepository.findByMemberId(memberId) } returns null

            it("When: 로그아웃 시 Then: INVALID_REFRESH_TOKEN 예외가 발생한다") {
                shouldThrow<GlobalException> {
                    service.signout("Bearer unknown-token")
                }.errorCode shouldBe ErrorCode.INVALID_REFRESH_TOKEN
            }
        }

        context("Given: Bearer 접두사 없는 토큰") {
            val claims = validClaims()
            every { jwtProvider.parseClaims(AuthFixture.VALID_REFRESH_TOKEN) } returns claims
            every { refreshTokenRepository.findByMemberId(memberId) } returns AuthFixture.VALID_REFRESH_TOKEN
            justRun { refreshTokenRepository.deleteByMemberId(memberId) }

            it("When: 로그아웃 시 Then: Bearer 없이도 정상 처리된다") {
                service.signout(AuthFixture.VALID_REFRESH_TOKEN)
                verify(exactly = 1) { refreshTokenRepository.deleteByMemberId(memberId) }
            }
        }
    }
})