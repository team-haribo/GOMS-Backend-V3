package com.example.team.haribo.goms.domain.auth.service.impl

import com.example.team.haribo.goms.domain.auth.dto.request.SignupRequest
import com.example.team.haribo.goms.domain.auth.repository.EmailVerificationRepository
import com.example.team.haribo.goms.domain.auth.service.SignupService
import com.example.team.haribo.goms.domain.auth.util.AuthValidators
import com.example.team.haribo.goms.domain.common.enums.Purpose
import com.example.team.haribo.goms.domain.common.enums.Role
import com.example.team.haribo.goms.domain.member.entity.Member
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class SignupServiceImpl(
    private val memberRepository: MemberRepository,
    private val emailVerificationRepository: EmailVerificationRepository,
    private val passwordEncoder: PasswordEncoder
) : SignupService {

    @Transactional
    override fun signup(request: SignupRequest) {
        if (memberRepository.existsByEmail(request.email)) {
            throw GlobalException(ErrorCode.ALREADY_REGISTERED_EMAIL)
        }

        val verification = emailVerificationRepository
            .findByEmailAndPurpose(request.email, Purpose.SIGNUP)
            .orElseThrow { GlobalException(ErrorCode.INVALID_VERIFIED_TOKEN) }

        val isTokenMismatched = verification.verifiedToken != request.verifiedToken
        val isExpired = verification.verifiedTokenExpiresAt?.isBefore(LocalDateTime.now()) ?: true
        if (isTokenMismatched || isExpired) {
            throw GlobalException(ErrorCode.INVALID_VERIFIED_TOKEN)
        }

        AuthValidators.validatePassword(request.password)

        val encoded = passwordEncoder.encode(request.password)
            ?: throw GlobalException(ErrorCode.INVALID_PASSWORD_POLICY)

        val member = Member(
            email = request.email,
            password = encoded,
            name = request.name,
            grade = request.grade,
            department = request.department,
            gender = request.gender,
            role = Role.ROLE_STUDENT
        )

        memberRepository.save(member)
    }
}
