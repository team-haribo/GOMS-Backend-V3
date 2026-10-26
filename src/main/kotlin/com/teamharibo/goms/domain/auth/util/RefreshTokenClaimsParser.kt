package com.teamharibo.goms.domain.auth.util

import com.teamharibo.goms.global.exception.ErrorCode
import com.teamharibo.goms.global.exception.GlobalException
import com.teamharibo.goms.global.jwt.JwtProvider
import com.teamharibo.goms.global.log.LogFormat
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import org.slf4j.Logger

object RefreshTokenClaimsParser {

    fun parse(jwtProvider: JwtProvider, token: String, log: Logger, failureEvent: String): Claims {
        try {
            return jwtProvider.parseClaims(token)
        } catch (e: ExpiredJwtException) {
            log.warn(
                LogFormat.message(
                    domain = "AUTH",
                    event = failureEvent,
                    "reason" to "만료된 리프레시 토큰"
                )
            )
            throw GlobalException(ErrorCode.EXPIRED_TOKEN)
        } catch (e: JwtException) {
            log.warn(
                LogFormat.message(
                    domain = "AUTH",
                    event = failureEvent,
                    "reason" to "유효하지 않은 리프레시 토큰 서명 또는 형식",
                    "exception" to e.javaClass.simpleName
                )
            )
            throw GlobalException(ErrorCode.INVALID_REFRESH_TOKEN)
        } catch (e: IllegalArgumentException) {
            log.warn(
                LogFormat.message(
                    domain = "AUTH",
                    event = failureEvent,
                    "reason" to "유효하지 않은 리프레시 토큰 형식",
                    "exception" to e.javaClass.simpleName
                )
            )
            throw GlobalException(ErrorCode.INVALID_REFRESH_TOKEN)
        }
    }
}
