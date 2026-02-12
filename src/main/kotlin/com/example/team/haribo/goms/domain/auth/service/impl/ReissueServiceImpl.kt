package com.example.team.haribo.goms.domain.auth.service.impl

import com.example.team.haribo.goms.domain.auth.dto.response.TokenResponse
import com.example.team.haribo.goms.domain.auth.repository.AuthRefreshTokenRepository
import com.example.team.haribo.goms.domain.auth.service.ReissueService
import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException
import com.example.team.haribo.goms.global.jwt.JwtProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ReissueServiceImpl(
    private val refreshTokenRepository: AuthRefreshTokenRepository,
    private val jwtProvider: JwtProvider
) : ReissueService {

    @Transactional
    override fun reissue(refreshTokenHeader: String): TokenResponse {
        val token = refreshTokenHeader.removePrefix("Bearer ").trim()

        val claims = jwtProvider.parseClaims(token)

        if (claims["type"] != "REFRESH") {
            throw GlobalException(ErrorCode.INVALID_REFRESH_TOKEN)
        }

        val memberId = claims.subject.toLong()

        val stored = refreshTokenRepository.findByMemberId(memberId)
            .orElseThrow { GlobalException(ErrorCode.INVALID_REFRESH_TOKEN) }

        val isTokenMismatched = stored.refreshToken != token
        val isExpired = stored.expiresAt.isBefore(LocalDateTime.now())
        val isRevoked = stored.revokedAt != null

        if (isTokenMismatched || isExpired || isRevoked) {
            throw GlobalException(ErrorCode.INVALID_REFRESH_TOKEN)
        }

        val role = claims["role"]?.toString() ?: "ROLE_STUDENT"

        val newAccess = jwtProvider.createAccessToken(memberId, role)
        val newRefresh = jwtProvider.createRefreshToken(memberId)

        stored.refreshToken = newRefresh
        stored.expiresAt = jwtProvider.getRefreshExpirationDate()
        stored.revokedAt = null

        refreshTokenRepository.save(stored)

        return TokenResponse(
            accessToken = newAccess,
            refreshToken = newRefresh,
            accessTokenExpiresIn = jwtProvider.getAccessExpirationDate(),
            refreshTokenExpiresIn = jwtProvider.getRefreshExpirationDate()
        )
    }
}
