package com.example.team.haribo.goms.domain.auth.service.impl

import com.example.team.haribo.goms.domain.auth.dto.response.TokenResponse
import com.example.team.haribo.goms.domain.auth.repository.redis.RefreshTokenRedisRepository
import com.example.team.haribo.goms.domain.auth.service.ReissueService
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException
import com.example.team.haribo.goms.global.jwt.JwtProvider
import com.example.team.haribo.goms.global.log.LogFormat
import org.slf4j.LoggerFactory
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

    private val log = LoggerFactory.getLogger(ReissueServiceImpl::class.java)

    @Transactional(readOnly = true)
    override fun reissue(refreshTokenHeader: String): TokenResponse {
        log.info(
            LogFormat.message(
                domain = "AUTH",
                event = "토큰 재발급 시도"
            )
        )

        val token = refreshTokenHeader.removePrefix("Bearer ").trim()
        val claims = jwtProvider.parseClaims(token)

        if (claims["type"] != "REFRESH") {
            log.warn(
                LogFormat.message(
                    domain = "AUTH",
                    event = "토큰 재발급 실패",
                    "reason" to "유효하지 않은 리프레시 토큰 타입"
                )
            )
            throw GlobalException(ErrorCode.INVALID_REFRESH_TOKEN)
        }

        val memberId = claims.subject.toLong()

        val storedToken = refreshTokenRedisRepository.findByMemberId(memberId)
            ?: run {
                log.warn(
                    LogFormat.message(
                        domain = "AUTH",
                        event = "토큰 재발급 실패",
                        "memberId" to memberId,
                        "reason" to "저장된 리프레시 토큰 없음"
                    )
                )
                throw GlobalException(ErrorCode.INVALID_REFRESH_TOKEN)
            }

        if (storedToken != token) {
            log.warn(
                LogFormat.message(
                    domain = "AUTH",
                    event = "토큰 재발급 실패",
                    "memberId" to memberId,
                    "reason" to "리프레시 토큰 불일치"
                )
            )
            throw GlobalException(ErrorCode.INVALID_REFRESH_TOKEN)
        }

        val member = memberRepository.findById(memberId)
            .orElseThrow {
                log.warn(
                    LogFormat.message(
                        domain = "AUTH",
                        event = "토큰 재발급 실패",
                        "memberId" to memberId,
                        "reason" to "존재하지 않는 사용자"
                    )
                )
                GlobalException(ErrorCode.NOT_FOUND_MEMBER)
            }

        val newAccessToken = jwtProvider.createAccessToken(memberId, member.role.name)
        val newRefreshToken = jwtProvider.createRefreshToken(memberId)
        val accessExpiresAt = jwtProvider.getAccessExpirationDate()
        val refreshExpiresAt = jwtProvider.getRefreshExpirationDate()

        val refreshTtlSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), refreshExpiresAt)
            .coerceAtLeast(1)

        refreshTokenRedisRepository.save(memberId, newRefreshToken, refreshTtlSeconds)

        log.info(
            LogFormat.message(
                domain = "AUTH",
                event = "토큰 재발급 완료",
                "memberId" to memberId,
                "role" to member.role
            )
        )

        return TokenResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
            accessTokenExpiresIn = accessExpiresAt,
            refreshTokenExpiresIn = refreshExpiresAt
        )
    }
}