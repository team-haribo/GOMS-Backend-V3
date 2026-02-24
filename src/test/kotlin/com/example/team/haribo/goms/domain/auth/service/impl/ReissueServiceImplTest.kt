package com.example.team.haribo.goms.domain.auth.service.impl

import com.example.team.haribo.goms.domain.auth.repository.AuthRefreshTokenRepository
import com.example.team.haribo.goms.fixture.AuthFixture
import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException
import com.example.team.haribo.goms.global.jwt.JwtProvider
import io.jsonwebtoken.Claims
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime
import java.util.Optional

class ReissueServiceImplTest : DescribeSpec({

    val refreshTokenRepository = mockk<AuthRefreshTokenRepository>()
    val jwtProvider = mockk<JwtProvider>()
    val service = ReissueServiceImpl(refreshTokenRepository, jwtProvider)

    val memberId = 1L
    val bearerHeader = "Bearer ${AuthFixture.VALID_REFRESH_TOKEN}"

    fun validClaims(): Claims = mockk<Claims>().also { claims ->
        every { claims["type"] } returns "REFRESH"
        every { claims.subject } returns memberId.toString()
        every { claims["role"] } returns "ROLE_STUDENT"
    }

    describe("ReissueService") {

        context("Given: 유효한 리프레시 토큰") {
            val stored = AuthFixture.activeRefreshToken(memberId)
            val claims = validClaims()
            every { jwtProvider.parseClaims(AuthFixture.VALID_REFRESH_TOKEN) } returns claims
            every { refreshTokenRepository.findByMemberId(memberId) } returns Optional.of(stored)
            every { jwtProvider.createAccessToken(memberId, "ROLE_STUDENT") } returns "new-access"
            every { jwtProvider.createRefreshToken(memberId) } returns "new-refresh"
            every { jwtProvider.getAccessExpirationDate() } returns LocalDateTime.now().plusHours(2)
            every { jwtProvider.getRefreshExpirationDate() } returns LocalDateTime.now().plusDays(7)
            every { refreshTokenRepository.save(any()) } returnsArgument 0

            it("When: 재발급 시 Then: 새 토큰을 반환하고 저장된 토큰을 갱신한다") {
                val response = service.reissue(bearerHeader)
                response.accessToken shouldBe "new-access"
                response.refreshToken shouldBe "new-refresh"
                stored.refreshToken shouldBe "new-refresh"
                verify(exactly = 1) { refreshTokenRepository.save(stored) }
            }
        }

        context("Given: 토큰 타입이 REFRESH가 아님") {
            val claims = mockk<Claims>()
            every { claims["type"] } returns "ACCESS"
            every { jwtProvider.parseClaims(any()) } returns claims

            it("When: 재발급 시 Then: INVALID_REFRESH_TOKEN 예외가 발생한다") {
                shouldThrow<GlobalException> {
                    service.reissue(bearerHeader)
                }.errorCode shouldBe ErrorCode.INVALID_REFRESH_TOKEN
            }
        }

        context("Given: DB에 저장된 토큰 없음") {
            val claims = validClaims()
            every { jwtProvider.parseClaims(any()) } returns claims
            every { refreshTokenRepository.findByMemberId(memberId) } returns Optional.empty()

            it("When: 재발급 시 Then: INVALID_REFRESH_TOKEN 예외가 발생한다") {
                shouldThrow<GlobalException> {
                    service.reissue(bearerHeader)
                }.errorCode shouldBe ErrorCode.INVALID_REFRESH_TOKEN
            }
        }

        context("Given: 저장 토큰과 요청 토큰 불일치") {
            val stored = AuthFixture.activeRefreshToken(memberId).also {
                it.refreshToken = "different-stored-token"
            }
            val claims = validClaims()
            every { jwtProvider.parseClaims(any()) } returns claims
            every { refreshTokenRepository.findByMemberId(memberId) } returns Optional.of(stored)

            it("When: 재발급 시 Then: INVALID_REFRESH_TOKEN 예외가 발생한다") {
                shouldThrow<GlobalException> {
                    service.reissue(bearerHeader)
                }.errorCode shouldBe ErrorCode.INVALID_REFRESH_TOKEN
            }
        }

        context("Given: 만료된 토큰 (expiresAt = 과거)") {
            val stored = AuthFixture.expiredRefreshToken(memberId)
            val claims = validClaims()
            every { jwtProvider.parseClaims(any()) } returns claims
            every { refreshTokenRepository.findByMemberId(memberId) } returns Optional.of(stored)

            it("When: 재발급 시 Then: INVALID_REFRESH_TOKEN 예외가 발생한다") {
                shouldThrow<GlobalException> {
                    service.reissue(bearerHeader)
                }.errorCode shouldBe ErrorCode.INVALID_REFRESH_TOKEN
            }
        }

        context("Given: 폐기된 토큰 (revokedAt != null)") {
            val stored = AuthFixture.revokedRefreshToken(memberId)
            val claims = validClaims()
            every { jwtProvider.parseClaims(any()) } returns claims
            every { refreshTokenRepository.findByMemberId(memberId) } returns Optional.of(stored)

            it("When: 재발급 시 Then: INVALID_REFRESH_TOKEN 예외가 발생한다") {
                shouldThrow<GlobalException> {
                    service.reissue(bearerHeader)
                }.errorCode shouldBe ErrorCode.INVALID_REFRESH_TOKEN
            }
        }
    }
})
