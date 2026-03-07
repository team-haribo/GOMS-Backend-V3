package com.example.team.haribo.goms.domain.auth.service.impl

import com.example.team.haribo.goms.domain.auth.dto.request.PasswordChangeRequest
import com.example.team.haribo.goms.domain.auth.exception.InvalidPasswordPolicyException
import com.example.team.haribo.goms.domain.auth.repository.redis.VerifiedTokenRedisRepository
import com.example.team.haribo.goms.domain.common.enums.Purpose
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.fixture.AuthFixture
import com.example.team.haribo.goms.fixture.MemberFixture
import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.Optional

class PasswordChangeServiceImplTest : DescribeSpec({

    val memberRepository = mockk<MemberRepository>()
    val verifiedTokenRedisRepository = mockk<VerifiedTokenRedisRepository>()
    val passwordEncoder = mockk<PasswordEncoder>()
    val service = PasswordChangeServiceImpl(memberRepository, verifiedTokenRedisRepository, passwordEncoder)

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
            every { verifiedTokenRedisRepository.find(email, Purpose.PASSWORD_CHANGE) } returns AuthFixture.VERIFIED_TOKEN
            every { passwordEncoder.encode(any()) } returns "new_encoded_password"
            justRun { verifiedTokenRedisRepository.delete(email, Purpose.PASSWORD_CHANGE) }

            it("When: 비밀번호 변경 시 Then: 인코딩된 비밀번호로 저장한다") {
                service.changePassword(validRequest)
                member.password shouldBe "new_encoded_password"
                verify(exactly = 1) { verifiedTokenRedisRepository.delete(email, Purpose.PASSWORD_CHANGE) }
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

        context("Given: 인증 토큰 없음") {
            every { memberRepository.findByEmail(email) } returns Optional.of(member)
            every { verifiedTokenRedisRepository.find(email, Purpose.PASSWORD_CHANGE) } returns null

            it("When: 비밀번호 변경 시 Then: INVALID_VERIFIED_TOKEN 예외가 발생한다") {
                shouldThrow<GlobalException> {
                    service.changePassword(validRequest)
                }.errorCode shouldBe ErrorCode.INVALID_VERIFIED_TOKEN
            }
        }

        context("Given: 토큰 불일치") {
            every { memberRepository.findByEmail(email) } returns Optional.of(member)
            every { verifiedTokenRedisRepository.find(email, Purpose.PASSWORD_CHANGE) } returns "wrong-token"

            it("When: 비밀번호 변경 시 Then: INVALID_VERIFIED_TOKEN 예외가 발생한다") {
                shouldThrow<GlobalException> {
                    service.changePassword(validRequest)
                }.errorCode shouldBe ErrorCode.INVALID_VERIFIED_TOKEN
            }
        }

        context("Given: 토큰 만료") {
            every { memberRepository.findByEmail(email) } returns Optional.of(member)
            every { verifiedTokenRedisRepository.find(email, Purpose.PASSWORD_CHANGE) } returns null

            it("When: 비밀번호 변경 시 Then: INVALID_VERIFIED_TOKEN 예외가 발생한다") {
                shouldThrow<GlobalException> {
                    service.changePassword(validRequest)
                }.errorCode shouldBe ErrorCode.INVALID_VERIFIED_TOKEN
            }
        }

        context("Given: 비밀번호 정책 위반 (문자 없는 숫자만)") {
            every { memberRepository.findByEmail(email) } returns Optional.of(member)
            every { verifiedTokenRedisRepository.find(email, Purpose.PASSWORD_CHANGE) } returns AuthFixture.VERIFIED_TOKEN

            it("When: 비밀번호 변경 시 Then: InvalidPasswordPolicyException이 발생한다") {
                shouldThrow<InvalidPasswordPolicyException> {
                    service.changePassword(validRequest.copy(newPassword = "123456"))
                }
            }
        }
    }
})