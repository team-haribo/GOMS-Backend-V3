package com.example.team.haribo.goms.domain.auth.service.impl

import com.example.team.haribo.goms.domain.auth.dto.request.SignupRequest
import com.example.team.haribo.goms.domain.auth.exception.InvalidPasswordPolicyException
import com.example.team.haribo.goms.domain.auth.repository.EmailVerificationRepository
import com.example.team.haribo.goms.domain.common.enums.Department
import com.example.team.haribo.goms.domain.common.enums.Gender
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.fixture.AuthFixture
import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.Optional

class SignupServiceImplTest : DescribeSpec({

    val memberRepository = mockk<MemberRepository>()
    val emailVerificationRepository = mockk<EmailVerificationRepository>()
    val passwordEncoder = mockk<PasswordEncoder>()
    val service = SignupServiceImpl(memberRepository, emailVerificationRepository, passwordEncoder)

    val validRequest = SignupRequest(
        email = "student@gsm.hs.kr",
        verifiedToken = AuthFixture.VERIFIED_TOKEN,
        password = "Password1",
        name = "홍길동",
        grade = 1L,
        department = Department.SW,
        gender = Gender.MALE
    )

    describe("SignupService") {

        context("Given: 유효한 가입 요청") {
            every { memberRepository.existsByEmail(any()) } returns false
            every { emailVerificationRepository.findByEmailAndPurpose(any(), any()) } returns
                Optional.of(AuthFixture.signupVerification())
            every { passwordEncoder.encode(any()) } returns "encoded_password"
            every { memberRepository.save(any()) } returnsArgument 0

            it("When: 회원가입 시 Then: 멤버를 저장한다") {
                service.signup(validRequest)
                verify(exactly = 1) { memberRepository.save(any()) }
            }
        }

        context("Given: 이미 등록된 이메일") {
            every { memberRepository.existsByEmail(any()) } returns true

            it("When: 회원가입 시 Then: ALREADY_REGISTERED_EMAIL 예외가 발생한다") {
                shouldThrow<GlobalException> {
                    service.signup(validRequest)
                }.errorCode shouldBe ErrorCode.ALREADY_REGISTERED_EMAIL
            }
        }

        context("Given: 이메일 인증 레코드가 없는 경우") {
            every { memberRepository.existsByEmail(any()) } returns false
            every { emailVerificationRepository.findByEmailAndPurpose(any(), any()) } returns Optional.empty()

            it("When: 회원가입 시 Then: INVALID_VERIFIED_TOKEN 예외가 발생한다") {
                shouldThrow<GlobalException> {
                    service.signup(validRequest)
                }.errorCode shouldBe ErrorCode.INVALID_VERIFIED_TOKEN
            }
        }

        context("Given: 인증 토큰 불일치") {
            every { memberRepository.existsByEmail(any()) } returns false
            every { emailVerificationRepository.findByEmailAndPurpose(any(), any()) } returns
                Optional.of(AuthFixture.signupVerification(verifiedToken = "different-token"))

            it("When: 회원가입 시 Then: INVALID_VERIFIED_TOKEN 예외가 발생한다") {
                shouldThrow<GlobalException> {
                    service.signup(validRequest)
                }.errorCode shouldBe ErrorCode.INVALID_VERIFIED_TOKEN
            }
        }

        context("Given: 인증 토큰 만료") {
            every { memberRepository.existsByEmail(any()) } returns false
            every { emailVerificationRepository.findByEmailAndPurpose(any(), any()) } returns
                Optional.of(AuthFixture.expiredVerifiedTokenVerification())

            it("When: 회원가입 시 Then: INVALID_VERIFIED_TOKEN 예외가 발생한다") {
                shouldThrow<GlobalException> {
                    service.signup(validRequest.copy(verifiedToken = AuthFixture.VERIFIED_TOKEN))
                }.errorCode shouldBe ErrorCode.INVALID_VERIFIED_TOKEN
            }
        }

        context("Given: 비밀번호 정책 위반 (문자 없는 숫자만)") {
            every { memberRepository.existsByEmail(any()) } returns false
            every { emailVerificationRepository.findByEmailAndPurpose(any(), any()) } returns
                Optional.of(AuthFixture.signupVerification())

            it("When: 회원가입 시 Then: InvalidPasswordPolicyException이 발생한다") {
                shouldThrow<InvalidPasswordPolicyException> {
                    service.signup(validRequest.copy(password = "123456"))
                }
            }
        }
    }
})
