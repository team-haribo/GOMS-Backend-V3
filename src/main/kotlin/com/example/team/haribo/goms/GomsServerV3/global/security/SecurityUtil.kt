package com.example.team.haribo.goms.GomsServerV3.global.security

import com.example.team.haribo.goms.GomsServerV3.global.exception.ErrorCode
import com.example.team.haribo.goms.GomsServerV3.global.exception.GlobalException
import org.springframework.security.core.context.SecurityContextHolder

object SecurityUtil {

    fun getCurrentMemberId(): Long {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw GlobalException(ErrorCode.INVALID_MEMBER_PRINCIPAL)

        val principal = authentication.principal
            ?: throw GlobalException(ErrorCode.INVALID_MEMBER_PRINCIPAL)

        return when (principal) {
            is Long -> principal
            is Int -> principal.toLong()
            is String -> principal.toLongOrNull()
                ?: throw GlobalException(ErrorCode.INVALID_MEMBER_PRINCIPAL)
            else -> throw GlobalException(ErrorCode.INVALID_MEMBER_PRINCIPAL)
        }
    }
}
