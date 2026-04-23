package com.example.team.haribo.goms.domain.auth.service.impl

import com.example.team.haribo.goms.domain.auth.dto.request.EmailVerificationConfirmRequest
import com.example.team.haribo.goms.domain.auth.dto.request.EmailVerificationSendRequest
import com.example.team.haribo.goms.domain.auth.dto.response.EmailVerificationConfirmResponse
import com.example.team.haribo.goms.domain.auth.exception.EmailAlreadyExistsException
import com.example.team.haribo.goms.domain.auth.exception.NotFoundUserException
import com.example.team.haribo.goms.domain.auth.exception.TooManyRequestsException
import com.example.team.haribo.goms.domain.auth.exception.VerificationCodeExpiredException
import com.example.team.haribo.goms.domain.auth.exception.VerificationCodeMismatchException
import com.example.team.haribo.goms.domain.auth.repository.redis.EmailVerificationCodeRedisRepository
import com.example.team.haribo.goms.domain.auth.repository.redis.VerifiedTokenRedisRepository
import com.example.team.haribo.goms.domain.auth.service.EmailVerificationService
import com.example.team.haribo.goms.domain.auth.util.AuthValidators
import com.example.team.haribo.goms.domain.auth.util.EmailSender
import com.example.team.haribo.goms.domain.common.enums.Purpose
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.global.log.LogFormat
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.UUID

@Service
class EmailVerificationServiceImpl(
    private val emailVerificationCodeRedisRepository: EmailVerificationCodeRedisRepository,
    private val verifiedTokenRedisRepository: VerifiedTokenRedisRepository,
    private val memberRepository: MemberRepository,
    private val emailSender: EmailSender
) : EmailVerificationService {

    private val log = LoggerFactory.getLogger(EmailVerificationServiceImpl::class.java)

    override fun send(request: EmailVerificationSendRequest) {
        AuthValidators.validateEmail(request.email)

        log.info(
            LogFormat.message(
                domain = "AUTH",
                event = "인증코드 발송 시도",
                "email" to request.email,
                "purpose" to request.purpose
            )
        )

        if (request.purpose == Purpose.SIGNUP && memberRepository.existsByEmail(request.email)) {
            log.warn(
                LogFormat.message(
                    domain = "AUTH",
                    event = "인증코드 발송 실패",
                    "email" to request.email,
                    "purpose" to request.purpose,
                    "reason" to "이미 가입된 이메일"
                )
            )
            throw EmailAlreadyExistsException()
        }

        if (request.purpose == Purpose.PASSWORD_CHANGE && !memberRepository.existsByEmail(request.email)) {
            log.warn(
                LogFormat.message(
                    domain = "AUTH",
                    event = "인증코드 발송 실패",
                    "email" to request.email,
                    "purpose" to request.purpose,
                    "reason" to "존재하지 않는 사용자"
                )
            )
            throw NotFoundUserException()
        }

        if (emailVerificationCodeRedisRepository.existsCooldown(request.email, request.purpose)) {
            log.warn(
                LogFormat.message(
                    domain = "AUTH",
                    event = "인증코드 발송 제한",
                    "email" to request.email,
                    "purpose" to request.purpose,
                    "reason" to "재요청 제한"
                )
            )
            throw TooManyRequestsException()
        }

        val code = (SecureRandom().nextInt(900000) + 100000).toString()

        emailVerificationCodeRedisRepository.save(request.email, request.purpose, code, 300)
        emailVerificationCodeRedisRepository.saveCooldown(request.email, request.purpose, 60)

        emailSender.sendVerificationCode(request.email, code)

        log.info(
            LogFormat.message(
                domain = "AUTH",
                event = "인증코드 발송 완료",
                "email" to request.email,
                "purpose" to request.purpose
            )
        )
    }

    override fun confirm(request: EmailVerificationConfirmRequest): EmailVerificationConfirmResponse {
        log.info(
            LogFormat.message(
                domain = "AUTH",
                event = "이메일 인증 시도",
                "email" to request.email,
                "purpose" to request.purpose
            )
        )

        if (emailVerificationCodeRedisRepository.getConfirmFailCount(request.email, request.purpose) >= 5) {
            log.warn(
                LogFormat.message(
                    domain = "AUTH",
                    event = "이메일 인증 차단",
                    "email" to request.email,
                    "purpose" to request.purpose,
                    "reason" to "실패 횟수 초과"
                )
            )
            throw TooManyRequestsException()
        }

        val storedCode = emailVerificationCodeRedisRepository.find(request.email, request.purpose)
            ?: run {
                log.warn(
                    LogFormat.message(
                        domain = "AUTH",
                        event = "이메일 인증 실패",
                        "email" to request.email,
                        "purpose" to request.purpose,
                        "reason" to "인증코드 만료"
                    )
                )
                throw VerificationCodeExpiredException()
            }

        if (storedCode != request.code) {
            val failCount = emailVerificationCodeRedisRepository.increaseConfirmFailCount(
                request.email,
                request.purpose,
                300
            )

            if (failCount >= 5) {
                emailVerificationCodeRedisRepository.delete(request.email, request.purpose)
                emailVerificationCodeRedisRepository.deleteConfirmFailCount(request.email, request.purpose)

                log.warn(
                    LogFormat.message(
                        domain = "AUTH",
                        event = "이메일 인증 차단",
                        "email" to request.email,
                        "purpose" to request.purpose,
                        "reason" to "실패 횟수 초과"
                    )
                )
                throw TooManyRequestsException()
            }

            log.warn(
                LogFormat.message(
                    domain = "AUTH",
                    event = "이메일 인증 실패",
                    "email" to request.email,
                    "purpose" to request.purpose,
                    "reason" to "코드 불일치",
                    "failCount" to failCount
                )
            )
            throw VerificationCodeMismatchException()
        }

        val verifiedToken = UUID.randomUUID().toString()
        val expiresAt = LocalDateTime.now().plusMinutes(10)

        verifiedTokenRedisRepository.save(request.email, request.purpose, verifiedToken, 600)
        emailVerificationCodeRedisRepository.delete(request.email, request.purpose)
        emailVerificationCodeRedisRepository.deleteConfirmFailCount(request.email, request.purpose)

        log.info(
            LogFormat.message(
                domain = "AUTH",
                event = "이메일 인증 성공",
                "email" to request.email,
                "purpose" to request.purpose
            )
        )

        return EmailVerificationConfirmResponse(
            verifiedToken = verifiedToken,
            verifiedTokenExpiresIn = expiresAt
        )
    }
}