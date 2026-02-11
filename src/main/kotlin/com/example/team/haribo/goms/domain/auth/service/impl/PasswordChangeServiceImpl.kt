package com.example.team.haribo.goms.domain.auth.service.impl

import com.example.team.haribo.goms.domain.auth.dto.request.PasswordChangeRequest
import com.example.team.haribo.goms.domain.auth.repository.EmailVerificationRepository
import com.example.team.haribo.goms.domain.auth.service.PasswordChangeService
import com.example.team.haribo.goms.domain.auth.util.AuthValidators
import com.example.team.haribo.goms.domain.common.enums.Purpose
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PasswordChangeServiceImpl(
    private val memberRepository: MemberRepository,
    private val emailVerificationRepository: EmailVerificationRepository,
    private val passwordEncoder: PasswordEncoder
) : PasswordChangeService {

    @Transactional
    override fun changePassword(request: PasswordChangeRequest) {
        val member = memberRepository.findByEmail(request.email)
            .orElseThrow { GlobalException(ErrorCode.NOT_FOUND_MEMBER) }

        val verification = emailVerificationRepository
            .findByEmailAndPurpose(request.email, Purpose.PASSWORD_CHANGE)
            .orElseThrow { GlobalException(ErrorCode.INVALID_VERIFIED_TOKEN) }

        val isTokenMismatched = verification.verifiedToken != request.verifiedToken
        val isExpired = verification.verifiedTokenExpiresAt?.isBefore(LocalDateTime.now()) ?: true
        if (isTokenMismatched || isExpired) {
            throw GlobalException(ErrorCode.INVALID_VERIFIED_TOKEN)
        }

        AuthValidators.validatePassword(request.newPassword)

        val encoded = passwordEncoder.encode(request.newPassword)
            ?: throw GlobalException(ErrorCode.INVALID_PASSWORD_POLICY)

        member.password = encoded
    }
}
