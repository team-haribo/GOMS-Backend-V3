package com.example.team.haribo.goms.domain.auth.service.impl

import com.example.team.haribo.goms.domain.auth.dto.request.SigninRequest
import com.example.team.haribo.goms.domain.auth.exception.InvalidEmailFormatException
import com.example.team.haribo.goms.domain.auth.repository.AuthRefreshTokenRepository
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.fixture.AuthFixture
import com.example.team.haribo.goms.fixture.MemberFixture
import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException
import com.example.team.haribo.goms.global.jwt.JwtProvider
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime
import java.util.Optional

class SigninServiceImplTest : DescribeSpec({

    val memberRepository = mockk<MemberRepository>()
    val refreshTokenRepository = mockk<AuthRefreshTokenRepository>()
    val passwordEncoder = mockk<PasswordEncoder>()
    val jwtProvider = mockk<JwtProvider>()
    val service = SigninServiceImpl(memberRepository, refreshTokenRepository, passwordEncoder, jwtProvider)

    val member = MemberFixture.student()
    val validRequest = SigninRequest(email = "student@gsm.hs.kr", password = "Password1")

    describe("SigninService") {

        context("Given: 유효한 이메일·비밀번호 + 기존 토큰 없음") {
            every { memberRepository.findByEmail(any()) } returns Optional.of(member)
            every { passwordEncoder.matches(any(), any()) } returns true
            every { jwtProvider.createAccessToken(any(), any()) } returns "access-token"
            every { jwtProvider.createRefreshToken(any()) } returns "refresh-token"
            every { jwtProvider.getAccessExpirationDate() } returns LocalDateTime.now().plusHours(2)
            every { jwtProvider.getRefreshExpirationDate() } returns LocalDateTime.now().plusDays(7)
            every { refreshTokenRepository.findByMemberId(any()) } returns Optional.empty()
            every { refreshTokenRepository.save(any()) } returnsArgument 0

            it("When: 로그인 시 Then: 액세스·리프레시 토큰을 반환하고 새 토큰을 저장한다") {
                val response = service.signin(validRequest)
                response.accessToken shouldBe "access-token"
                response.refreshToken shouldBe "refresh-token"
                verify(exactly = 1) { refreshTokenRepository.save(any()) }
            }
        }

        context("Given: 유효한 이메일·비밀번호 + 기존 토큰 있음") {
            val existingToken = AuthFixture.activeRefreshToken(memberId = member.id!!)
            every { memberRepository.findByEmail(any()) } returns Optional.of(member)
            every { passwordEncoder.matches(any(), any()) } returns true
            every { jwtProvider.createAccessToken(any(), any()) } returns "new-access-token"
            every { jwtProvider.createRefreshToken(any()) } returns "new-refresh-token"
            every { jwtProvider.getAccessExpirationDate() } returns LocalDateTime.now().plusHours(2)
            every { jwtProvider.getRefreshExpirationDate() } returns LocalDateTime.now().plusDays(7)
            every { refreshTokenRepository.findByMemberId(any()) } returns Optional.of(existingToken)
            every { refreshTokenRepository.save(any()) } returnsArgument 0

            it("When: 로그인 시 Then: 기존 토큰을 갱신하고 revokedAt을 null로 설정한다") {
                val response = service.signin(validRequest)
                response.accessToken shouldBe "new-access-token"
                existingToken.revokedAt shouldBe null
                verify(exactly = 1) { refreshTokenRepository.save(existingToken) }
            }
        }

        context("Given: 존재하지 않는 이메일") {
            every { memberRepository.findByEmail(any()) } returns Optional.empty()

            it("When: 로그인 시 Then: NOT_FOUND_EMAIL 예외가 발생한다") {
                shouldThrow<GlobalException> {
                    service.signin(validRequest)
                }.errorCode shouldBe ErrorCode.NOT_FOUND_EMAIL
            }
        }

        context("Given: 비밀번호 불일치") {
            every { memberRepository.findByEmail(any()) } returns Optional.of(member)
            every { passwordEncoder.matches(any(), any()) } returns false

            it("When: 로그인 시 Then: PASSWORD_MISMATCH 예외가 발생한다") {
                shouldThrow<GlobalException> {
                    service.signin(validRequest.copy(password = "wrongPassword"))
                }.errorCode shouldBe ErrorCode.PASSWORD_MISMATCH
            }
        }

        context("Given: 이메일 형식 오류") {
            it("When: 잘못된 이메일 형식으로 로그인 시 Then: InvalidEmailFormatException이 발생한다") {
                shouldThrow<InvalidEmailFormatException> {
                    service.signin(SigninRequest(email = "not-an-email", password = "Password1"))
                }
            }
        }
    }
})
