package com.example.team.haribo.goms.domain.auth.service.impl

import com.example.team.haribo.goms.domain.auth.dto.request.EmailVerificationConfirmRequest
import com.example.team.haribo.goms.domain.auth.dto.request.EmailVerificationSendRequest
import com.example.team.haribo.goms.domain.auth.dto.response.EmailVerificationConfirmResponse
import com.example.team.haribo.goms.domain.auth.entity.EmailVerification
import com.example.team.haribo.goms.domain.auth.exception.EmailAlreadyExistsException
import com.example.team.haribo.goms.domain.auth.exception.NotFoundUserException
import com.example.team.haribo.goms.domain.auth.exception.TooManyRequestsException
import com.example.team.haribo.goms.domain.auth.exception.VerificationCodeExpiredException
import com.example.team.haribo.goms.domain.auth.exception.VerificationCodeMismatchException
import com.example.team.haribo.goms.domain.auth.repository.EmailVerificationRepository
import com.example.team.haribo.goms.domain.auth.service.EmailVerificationService
import com.example.team.haribo.goms.domain.auth.util.AuthValidators
import com.example.team.haribo.goms.domain.auth.util.EmailSender
import com.example.team.haribo.goms.domain.common.enums.Purpose
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class EmailVerificationServiceImpl(
    private val repository: EmailVerificationRepository,
    private val memberRepository: MemberRepository,
    private val emailSender: EmailSender
) : EmailVerificationService {

    @Transactional
    override fun send(request: EmailVerificationSendRequest) {
        AuthValidators.validateEmail(request.email)

        if (request.purpose == Purpose.SIGNUP && memberRepository.existsByEmail(request.email)) {
            throw EmailAlreadyExistsException()
        }

        if (request.purpose == Purpose.PASSWORD_CHANGE && !memberRepository.existsByEmail(request.email)) {
            throw NotFoundUserException()
        }

        val now = LocalDateTime.now()
        val code = (100000..999999).random().toString()

        val entity = repository.findByEmailAndPurpose(request.email, request.purpose).orElse(
            EmailVerification(
                email = request.email,
                purpose = request.purpose,
                code = code,
                codeExpiresAt = now.plusMinutes(5)
            )
        )

        if (entity.lastSentAt != null && entity.lastSentAt!!.plusSeconds(60).isAfter(now)) {
            throw TooManyRequestsException()
        }

        entity.code = code
        entity.codeExpiresAt = now.plusMinutes(5)
        entity.lastSentAt = now

        repository.save(entity)

        emailSender.sendVerificationCode(request.email, code)
    }

    @Transactional
    override fun confirm(request: EmailVerificationConfirmRequest): EmailVerificationConfirmResponse {
        val entity = repository.findByEmailAndPurpose(request.email, request.purpose)
            .orElseThrow { VerificationCodeExpiredException() }

        if (entity.codeExpiresAt.isBefore(LocalDateTime.now())) {
            throw VerificationCodeExpiredException()
        }

        if (entity.code != request.code) {
            throw VerificationCodeMismatchException()
        }

        val verifiedToken = UUID.randomUUID().toString()

        entity.verifiedToken = verifiedToken
        entity.verifiedTokenExpiresAt = LocalDateTime.now().plusMinutes(10)

        repository.save(entity)

        return EmailVerificationConfirmResponse(
            verifiedToken = verifiedToken,
            verifiedTokenExpiresIn = entity.verifiedTokenExpiresAt!!
        )
    }
}
