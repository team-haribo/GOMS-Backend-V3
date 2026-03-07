package com.example.team.haribo.goms.domain.auth.service.impl

import com.example.team.haribo.goms.domain.auth.repository.redis.RefreshTokenRedisRepository
import com.example.team.haribo.goms.domain.auth.service.SignoutService
import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException
import com.example.team.haribo.goms.global.jwt.JwtProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SignoutServiceImpl(
    private val refreshTokenRedisRepository: RefreshTokenRedisRepository,
    private val jwtProvider: JwtProvider
) : SignoutService {

    @Transactional
    override fun signout(refreshTokenHeader: String) {
        val token = refreshTokenHeader.removePrefix("Bearer ").trim()
        val claims = jwtProvider.parseClaims(token)

        if (claims["type"] != "REFRESH") {
            throw GlobalException(ErrorCode.INVALID_REFRESH_TOKEN)
        }

        val memberId = claims.subject.toLong()

        val storedToken = refreshTokenRedisRepository.findByMemberId(memberId)
            ?: throw GlobalException(ErrorCode.INVALID_REFRESH_TOKEN)

        if (storedToken != token) {
            throw GlobalException(ErrorCode.INVALID_REFRESH_TOKEN)
        }

        refreshTokenRedisRepository.deleteByMemberId(memberId)
    }
}