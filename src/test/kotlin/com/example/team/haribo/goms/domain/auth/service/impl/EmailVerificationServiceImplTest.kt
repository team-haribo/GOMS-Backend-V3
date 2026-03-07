package com.example.team.haribo.goms.domain.auth.service.impl

import com.example.team.haribo.goms.domain.auth.dto.request.EmailVerificationConfirmRequest
import com.example.team.haribo.goms.domain.auth.dto.request.EmailVerificationSendRequest
import com.example.team.haribo.goms.domain.auth.exception.EmailAlreadyExistsException
import com.example.team.haribo.goms.domain.auth.exception.NotFoundUserException
import com.example.team.haribo.goms.domain.auth.exception.TooManyRequestsException
import com.example.team.haribo.goms.domain.auth.exception.VerificationCodeExpiredException
import com.example.team.haribo.goms.domain.auth.exception.VerificationCodeMismatchException
import com.example.team.haribo.goms.domain.auth.repository.redis.EmailVerificationCodeRedisRepository
import com.example.team.haribo.goms.domain.auth.repository.redis.VerifiedTokenRedisRepository
import com.example.team.haribo.goms.domain.auth.util.EmailSender
import com.example.team.haribo.goms.domain.common.enums.Purpose
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify

class EmailVerificationServiceImplTest : DescribeSpec({

    val codeRedisRepository = mockk<EmailVerificationCodeRedisRepository>()
    val verifiedTokenRedisRepository = mockk<VerifiedTokenRedisRepository>()
    val memberRepository = mockk<MemberRepository>()
    val emailSender = mockk<EmailSender>()
    val service = EmailVerificationServiceImpl(
        codeRedisRepository,
        verifiedTokenRedisRepository,
        memberRepository,
        emailSender
    )

    val email = "student@gsm.hs.kr"

    afterEach { clearAllMocks() }

    describe("EmailVerificationService.send()") {

        context("Given: SIGNUP 목적 + 전송 가능한 상태") {
            beforeEach {
                every { memberRepository.existsByEmail(email) } returns false
                every { codeRedisRepository.existsCooldown(email, Purpose.SIGNUP) } returns false
                justRun { codeRedisRepository.save(email, Purpose.SIGNUP, any(), 300) }
                justRun { codeRedisRepository.saveCooldown(email, Purpose.SIGNUP, 60) }
                justRun { emailSender.sendVerificationCode(any(), any()) }
            }

            it("When: 인증 메일 전송 시 Then: 코드를 저장하고 메일을 발송한다") {
                service.send(EmailVerificationSendRequest(email, Purpose.SIGNUP))
                verify(exactly = 1) { codeRedisRepository.save(email, Purpose.SIGNUP, any(), 300) }
                verify(exactly = 1) { codeRedisRepository.saveCooldown(email, Purpose.SIGNUP, 60) }
                verify(exactly = 1) { emailSender.sendVerificationCode(email, any()) }
            }
        }

        context("Given: SIGNUP 목적 + 재발송 가능한 상태") {
            beforeEach {
                every { memberRepository.existsByEmail(email) } returns false
                every { codeRedisRepository.existsCooldown(email, Purpose.SIGNUP) } returns false
                justRun { codeRedisRepository.save(email, Purpose.SIGNUP, any(), 300) }
                justRun { codeRedisRepository.saveCooldown(email, Purpose.SIGNUP, 60) }
                justRun { emailSender.sendVerificationCode(any(), any()) }
            }

            it("When: 인증 메일 재전송 시 Then: 코드를 다시 저장하고 재발송한다") {
                service.send(EmailVerificationSendRequest(email, Purpose.SIGNUP))
                verify(exactly = 1) { codeRedisRepository.save(email, Purpose.SIGNUP, any(), 300) }
                verify(exactly = 1) { codeRedisRepository.saveCooldown(email, Purpose.SIGNUP, 60) }
                verify(exactly = 1) { emailSender.sendVerificationCode(email, any()) }
            }
        }

        context("Given: SIGNUP 목적 + 이미 등록된 이메일") {
            beforeEach {
                every { memberRepository.existsByEmail(email) } returns true
            }

            it("When: 인증 메일 전송 시 Then: EmailAlreadyExistsException이 발생한다") {
                shouldThrow<EmailAlreadyExistsException> {
                    service.send(EmailVerificationSendRequest(email, Purpose.SIGNUP))
                }
            }
        }

        context("Given: PASSWORD_CHANGE 목적 + 미존재 멤버") {
            beforeEach {
                every { memberRepository.existsByEmail(email) } returns false
            }

            it("When: 인증 메일 전송 시 Then: NotFoundUserException이 발생한다") {
                shouldThrow<NotFoundUserException> {
                    service.send(EmailVerificationSendRequest(email, Purpose.PASSWORD_CHANGE))
                }
            }
        }

        context("Given: 60초 내 재발송 시도") {
            beforeEach {
                every { memberRepository.existsByEmail(email) } returns false
                every { codeRedisRepository.existsCooldown(email, Purpose.SIGNUP) } returns true
            }

            it("When: 인증 메일 전송 시 Then: TooManyRequestsException이 발생한다") {
                shouldThrow<TooManyRequestsException> {
                    service.send(EmailVerificationSendRequest(email, Purpose.SIGNUP))
                }
            }
        }
    }

    describe("EmailVerificationService.confirm()") {

        context("Given: 유효한 코드") {
            beforeEach {
                every { codeRedisRepository.find(email, Purpose.SIGNUP) } returns "123456"
                justRun { verifiedTokenRedisRepository.save(email, Purpose.SIGNUP, any(), 600) }
                justRun { codeRedisRepository.delete(email, Purpose.SIGNUP) }
            }

            it("When: 코드 확인 시 Then: UUID 토큰을 반환하고 verifiedToken을 저장한다") {
                val response = service.confirm(
                    EmailVerificationConfirmRequest(email = email, code = "123456", purpose = Purpose.SIGNUP)
                )
                response.verifiedToken shouldNotBe null
                response.verifiedToken.length shouldBe 36
                verify(exactly = 1) { verifiedTokenRedisRepository.save(email, Purpose.SIGNUP, response.verifiedToken, 600) }
                verify(exactly = 1) { codeRedisRepository.delete(email, Purpose.SIGNUP) }
            }
        }

        context("Given: 코드 만료") {
            beforeEach {
                every { codeRedisRepository.find(email, Purpose.SIGNUP) } returns null
            }

            it("When: 코드 확인 시 Then: VerificationCodeExpiredException이 발생한다") {
                shouldThrow<VerificationCodeExpiredException> {
                    service.confirm(
                        EmailVerificationConfirmRequest(email = email, code = "123456", purpose = Purpose.SIGNUP)
                    )
                }
            }
        }

        context("Given: 코드 불일치") {
            beforeEach {
                every { codeRedisRepository.find(email, Purpose.SIGNUP) } returns "123456"
            }

            it("When: 잘못된 코드로 확인 시 Then: VerificationCodeMismatchException이 발생한다") {
                shouldThrow<VerificationCodeMismatchException> {
                    service.confirm(
                        EmailVerificationConfirmRequest(email = email, code = "999999", purpose = Purpose.SIGNUP)
                    )
                }
            }
        }
    }
})