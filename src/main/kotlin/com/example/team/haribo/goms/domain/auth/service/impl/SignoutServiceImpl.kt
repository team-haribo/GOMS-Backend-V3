package com.example.team.haribo.goms.domain.auth.service.impl

import com.example.team.haribo.goms.domain.auth.repository.AuthRefreshTokenRepository
import com.example.team.haribo.goms.domain.auth.service.SignoutService
import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class SignoutServiceImpl(
    private val refreshTokenRepository: AuthRefreshTokenRepository
) : SignoutService {

    @Transactional
    override fun signout(refreshTokenHeader: String) {
        val token = refreshTokenHeader.removePrefix("Bearer ").trim()

        val entity = refreshTokenRepository.findByRefreshToken(token)
            .orElseThrow { GlobalException(ErrorCode.INVALID_REFRESH_TOKEN) }

        entity.revokedAt = LocalDateTime.now()
    }
}
