package com.teamharibo.goms.domain.outing.util

import com.teamharibo.goms.domain.outing.exception.QrExpiredException
import java.time.Instant

object QrExpValidator {
    fun validate(exp: Long) {
        val expMillis = if (exp < 10_000_000_000L) exp * 1000 else exp
        if (Instant.now().toEpochMilli() > expMillis) {
            throw QrExpiredException()
        }
    }
}
