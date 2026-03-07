package com.example.team.haribo.goms.domain.auth.service.impl

import com.example.team.haribo.goms.domain.auth.dto.response.TokenResponse
import com.example.team.haribo.goms.domain.auth.repository.redis.RefreshTokenRedisRepository
import com.example.team.haribo.goms.domain.auth.service.ReissueService
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException
import com.example.team.haribo.goms.global.jwt.JwtProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Service
class ReissueServiceImpl(
    private val refreshTokenRedisRepository: RefreshTokenRedisRepository,
    private val memberRepository: MemberRepository,
    private val jwtProvider: JwtProvider
) : ReissueService {

    @Transactional(readOnly = true)
    override fun reissue(refreshTokenHeader: String): TokenResponse {
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

        val member = memberRepository.findById(memberId)
            .orElseThrow { GlobalException(ErrorCode.NOT_FOUND_MEMBER) }

        val newAccessToken = jwtProvider.createAccessToken(memberId, member.role.name)
        val newRefreshToken = jwtProvider.createRefreshToken(memberId)
        val accessExpiresAt = jwtProvider.getAccessExpirationDate()
        val refreshExpiresAt = jwtProvider.getRefreshExpirationDate()

        val refreshTtlSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), refreshExpiresAt)
            .coerceAtLeast(1)

        refreshTokenRedisRepository.save(memberId, newRefreshToken, refreshTtlSeconds)

        return TokenResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
            accessTokenExpiresIn = accessExpiresAt,
            refreshTokenExpiresIn = refreshExpiresAt
        )
    }
}