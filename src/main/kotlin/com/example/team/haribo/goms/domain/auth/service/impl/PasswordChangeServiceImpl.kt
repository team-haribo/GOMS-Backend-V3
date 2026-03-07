package com.example.team.haribo.goms.domain.auth.service.impl

import com.example.team.haribo.goms.domain.auth.dto.request.PasswordChangeRequest
import com.example.team.haribo.goms.domain.auth.repository.redis.VerifiedTokenRedisRepository
import com.example.team.haribo.goms.domain.auth.service.PasswordChangeService
import com.example.team.haribo.goms.domain.auth.util.AuthValidators
import com.example.team.haribo.goms.domain.common.enums.Purpose
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PasswordChangeServiceImpl(
    private val memberRepository: MemberRepository,
    private val verifiedTokenRedisRepository: VerifiedTokenRedisRepository,
    private val passwordEncoder: PasswordEncoder
) : PasswordChangeService {

    @Transactional
    override fun changePassword(request: PasswordChangeRequest) {
        val member = memberRepository.findByEmail(request.email)
            .orElseThrow { GlobalException(ErrorCode.NOT_FOUND_MEMBER) }

        val storedVerifiedToken = verifiedTokenRedisRepository.find(request.email, Purpose.PASSWORD_CHANGE)
            ?: throw GlobalException(ErrorCode.INVALID_VERIFIED_TOKEN)

        if (storedVerifiedToken != request.verifiedToken) {
            throw GlobalException(ErrorCode.INVALID_VERIFIED_TOKEN)
        }

        AuthValidators.validatePassword(request.newPassword)

        val encoded = passwordEncoder.encode(request.newPassword)
            ?: throw GlobalException(ErrorCode.INVALID_PASSWORD_POLICY)

        member.password = encoded
        verifiedTokenRedisRepository.delete(request.email, Purpose.PASSWORD_CHANGE)
    }
}