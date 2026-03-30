package com.example.team.haribo.goms.domain.auth.service.impl

import com.example.team.haribo.goms.domain.auth.repository.redis.RefreshTokenRedisRepository
import com.example.team.haribo.goms.domain.auth.service.SignoutService
import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException
import com.example.team.haribo.goms.global.jwt.JwtProvider
import com.example.team.haribo.goms.global.log.LogFormat
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SignoutServiceImpl(
    private val refreshTokenRedisRepository: RefreshTokenRedisRepository,
    private val jwtProvider: JwtProvider
) : SignoutService {

    private val log = LoggerFactory.getLogger(SignoutServiceImpl::class.java)

    override fun signout(refreshTokenHeader: String) {
        log.info(
            LogFormat.message(
                domain = "AUTH",
                event = "로그아웃 시도"
            )
        )

        val token = refreshTokenHeader.removePrefix("Bearer ").trim()
        val claims = jwtProvider.parseClaims(token)

        if (claims["type"] != "REFRESH") {
            log.warn(
                LogFormat.message(
                    domain = "AUTH",
                    event = "로그아웃 실패",
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
                        event = "로그아웃 실패",
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
                    event = "로그아웃 실패",
                    "memberId" to memberId,
                    "reason" to "리프레시 토큰 불일치"
                )
            )
            throw GlobalException(ErrorCode.INVALID_REFRESH_TOKEN)
        }

        refreshTokenRedisRepository.deleteByMemberId(memberId)

        log.info(
            LogFormat.message(
                domain = "AUTH",
                event = "로그아웃 완료",
                "memberId" to memberId
            )
        )
    }
}