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
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class EmailVerificationServiceImpl(
    private val emailVerificationCodeRedisRepository: EmailVerificationCodeRedisRepository,
    private val verifiedTokenRedisRepository: VerifiedTokenRedisRepository,
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

        if (emailVerificationCodeRedisRepository.existsCooldown(request.email, request.purpose)) {
            throw TooManyRequestsException()
        }

        val code = (100000..999999).random().toString()

        emailVerificationCodeRedisRepository.save(request.email, request.purpose, code, 300)
        emailVerificationCodeRedisRepository.saveCooldown(request.email, request.purpose, 60)

        emailSender.sendVerificationCode(request.email, code)
    }

    @Transactional
    override fun confirm(request: EmailVerificationConfirmRequest): EmailVerificationConfirmResponse {
        val storedCode = emailVerificationCodeRedisRepository.find(request.email, request.purpose)
            ?: throw VerificationCodeExpiredException()

        if (storedCode != request.code) {
            throw VerificationCodeMismatchException()
        }

        val verifiedToken = UUID.randomUUID().toString()
        val expiresAt = LocalDateTime.now().plusMinutes(10)

        verifiedTokenRedisRepository.save(request.email, request.purpose, verifiedToken, 600)
        emailVerificationCodeRedisRepository.delete(request.email, request.purpose)

        return EmailVerificationConfirmResponse(
            verifiedToken = verifiedToken,
            verifiedTokenExpiresIn = expiresAt
        )
    }
}