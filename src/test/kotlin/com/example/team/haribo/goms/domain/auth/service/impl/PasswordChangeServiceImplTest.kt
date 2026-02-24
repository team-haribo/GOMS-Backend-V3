package com.example.team.haribo.goms.domain.auth.service.impl

import com.example.team.haribo.goms.domain.auth.dto.request.PasswordChangeRequest
import com.example.team.haribo.goms.domain.auth.exception.InvalidPasswordPolicyException
import com.example.team.haribo.goms.domain.auth.repository.EmailVerificationRepository
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.fixture.AuthFixture
import com.example.team.haribo.goms.fixture.MemberFixture
import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.Optional

class PasswordChangeServiceImplTest : DescribeSpec({

    val memberRepository = mockk<MemberRepository>()
    val emailVerificationRepository = mockk<EmailVerificationRepository>()
    val passwordEncoder = mockk<PasswordEncoder>()
    val service = PasswordChangeServiceImpl(memberRepository, emailVerificationRepository, passwordEncoder)

    val email = "student@gsm.hs.kr"
    val member = MemberFixture.student()
    val validRequest = PasswordChangeRequest(
        email = email,
        verifiedToken = AuthFixture.VERIFIED_TOKEN,
        newPassword = "NewPass1"
    )

    describe("PasswordChangeService") {

        context("Given: 유효한 요청") {
            every { memberRepository.findByEmail(email) } returns Optional.of(member)
            every { emailVerificationRepository.findByEmailAndPurpose(any(), any()) } returns
                Optional.of(AuthFixture.passwordChangeVerification(email = email))
            every { passwordEncoder.encode(any()) } returns "new_encoded_password"

            it("When: 비밀번호 변경 시 Then: 인코딩된 비밀번호로 저장한다") {
                service.changePassword(validRequest)
                member.password shouldBe "new_encoded_password"
            }
        }

        context("Given: 존재하지 않는 이메일") {
            every { memberRepository.findByEmail(email) } returns Optional.empty()

            it("When: 비밀번호 변경 시 Then: NOT_FOUND_MEMBER 예외가 발생한다") {
                shouldThrow<GlobalException> {
                    service.changePassword(validRequest)
                }.errorCode shouldBe ErrorCode.NOT_FOUND_MEMBER
            }
        }

        context("Given: 인증 레코드 없음") {
            every { memberRepository.findByEmail(email) } returns Optional.of(member)
            every { emailVerificationRepository.findByEmailAndPurpose(any(), any()) } returns Optional.empty()

            it("When: 비밀번호 변경 시 Then: INVALID_VERIFIED_TOKEN 예외가 발생한다") {
                shouldThrow<GlobalException> {
                    service.changePassword(validRequest)
                }.errorCode shouldBe ErrorCode.INVALID_VERIFIED_TOKEN
            }
        }

        context("Given: 토큰 불일치") {
            every { memberRepository.findByEmail(email) } returns Optional.of(member)
            every { emailVerificationRepository.findByEmailAndPurpose(any(), any()) } returns
                Optional.of(AuthFixture.passwordChangeVerification(email = email, verifiedToken = "wrong-token"))

            it("When: 비밀번호 변경 시 Then: INVALID_VERIFIED_TOKEN 예외가 발생한다") {
                shouldThrow<GlobalException> {
                    service.changePassword(validRequest)
                }.errorCode shouldBe ErrorCode.INVALID_VERIFIED_TOKEN
            }
        }

        context("Given: 토큰 만료") {
            every { memberRepository.findByEmail(email) } returns Optional.of(member)
            every { emailVerificationRepository.findByEmailAndPurpose(any(), any()) } returns
                Optional.of(
                    AuthFixture.passwordChangeVerification(email = email).also {
                        it.verifiedTokenExpiresAt = java.time.LocalDateTime.now().minusMinutes(1)
                    }
                )

            it("When: 비밀번호 변경 시 Then: INVALID_VERIFIED_TOKEN 예외가 발생한다") {
                shouldThrow<GlobalException> {
                    service.changePassword(validRequest)
                }.errorCode shouldBe ErrorCode.INVALID_VERIFIED_TOKEN
            }
        }

        context("Given: 비밀번호 정책 위반 (문자 없는 숫자만)") {
            every { memberRepository.findByEmail(email) } returns Optional.of(member)
            every { emailVerificationRepository.findByEmailAndPurpose(any(), any()) } returns
                Optional.of(AuthFixture.passwordChangeVerification(email = email))

            it("When: 비밀번호 변경 시 Then: InvalidPasswordPolicyException이 발생한다") {
                shouldThrow<InvalidPasswordPolicyException> {
                    service.changePassword(validRequest.copy(newPassword = "123456"))
                }
            }
        }
    }
})
