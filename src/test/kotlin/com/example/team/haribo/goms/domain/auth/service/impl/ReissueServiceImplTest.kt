package com.example.team.haribo.goms.domain.auth.service.impl

import com.example.team.haribo.goms.domain.auth.repository.redis.RefreshTokenRedisRepository
import com.example.team.haribo.goms.domain.common.enums.Role
import com.example.team.haribo.goms.domain.member.entity.Member
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
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
import java.time.LocalDateTime
import java.util.Optional

class ReissueServiceImplTest : DescribeSpec({

    val refreshTokenRepository = mockk<RefreshTokenRedisRepository>()
    val memberRepository = mockk<MemberRepository>()
    val jwtProvider = mockk<JwtProvider>()
    val service = ReissueServiceImpl(refreshTokenRepository, memberRepository, jwtProvider)

    val memberId = 1L
    val bearerHeader = "Bearer ${AuthFixture.VALID_REFRESH_TOKEN}"

    fun validClaims(): Claims = mockk<Claims>().also { claims ->
        every { claims["type"] } returns "REFRESH"
        every { claims.subject } returns memberId.toString()
    }

    fun validMember(): Member = mockk<Member>().also { member ->
        every { member.id } returns memberId
        every { member.role } returns Role.ROLE_STUDENT
    }

    describe("ReissueService") {

        context("Given: 유효한 리프레시 토큰") {
            val claims = validClaims()
            val member = validMember()

            every { jwtProvider.parseClaims(AuthFixture.VALID_REFRESH_TOKEN) } returns claims
            every { refreshTokenRepository.findByMemberId(memberId) } returns AuthFixture.VALID_REFRESH_TOKEN
            every { memberRepository.findById(memberId) } returns Optional.of(member)
            every { jwtProvider.createAccessToken(memberId, "ROLE_STUDENT") } returns "new-access"
            every { jwtProvider.createRefreshToken(memberId) } returns "new-refresh"
            every { jwtProvider.getAccessExpirationDate() } returns LocalDateTime.now().plusHours(2)
            every { jwtProvider.getRefreshExpirationDate() } returns LocalDateTime.now().plusDays(7)
            justRun { refreshTokenRepository.save(memberId, "new-refresh", any()) }

            it("When: 재발급 시 Then: 새 토큰을 반환하고 저장된 토큰을 갱신한다") {
                val response = service.reissue(bearerHeader)
                response.accessToken shouldBe "new-access"
                response.refreshToken shouldBe "new-refresh"
                verify(exactly = 1) { refreshTokenRepository.save(memberId, "new-refresh", any()) }
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

        context("Given: 저장된 토큰 없음") {
            val claims = validClaims()
            every { jwtProvider.parseClaims(any()) } returns claims
            every { refreshTokenRepository.findByMemberId(memberId) } returns null

            it("When: 재발급 시 Then: INVALID_REFRESH_TOKEN 예외가 발생한다") {
                shouldThrow<GlobalException> {
                    service.reissue(bearerHeader)
                }.errorCode shouldBe ErrorCode.INVALID_REFRESH_TOKEN
            }
        }

        context("Given: 저장 토큰과 요청 토큰 불일치") {
            val claims = validClaims()
            every { jwtProvider.parseClaims(any()) } returns claims
            every { refreshTokenRepository.findByMemberId(memberId) } returns "different-stored-token"

            it("When: 재발급 시 Then: INVALID_REFRESH_TOKEN 예외가 발생한다") {
                shouldThrow<GlobalException> {
                    service.reissue(bearerHeader)
                }.errorCode shouldBe ErrorCode.INVALID_REFRESH_TOKEN
            }
        }

        context("Given: 만료된 토큰") {
            val claims = validClaims()
            every { jwtProvider.parseClaims(any()) } returns claims
            every { refreshTokenRepository.findByMemberId(memberId) } returns null

            it("When: 재발급 시 Then: INVALID_REFRESH_TOKEN 예외가 발생한다") {
                shouldThrow<GlobalException> {
                    service.reissue(bearerHeader)
                }.errorCode shouldBe ErrorCode.INVALID_REFRESH_TOKEN
            }
        }

        context("Given: 폐기된 토큰") {
            val claims = validClaims()
            every { jwtProvider.parseClaims(any()) } returns claims
            every { refreshTokenRepository.findByMemberId(memberId) } returns null

            it("When: 재발급 시 Then: INVALID_REFRESH_TOKEN 예외가 발생한다") {
                shouldThrow<GlobalException> {
                    service.reissue(bearerHeader)
                }.errorCode shouldBe ErrorCode.INVALID_REFRESH_TOKEN
            }
        }
    }
})