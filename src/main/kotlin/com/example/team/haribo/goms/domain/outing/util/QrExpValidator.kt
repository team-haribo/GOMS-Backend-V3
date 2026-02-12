package com.example.team.haribo.goms.domain.outing.util

import com.example.team.haribo.goms.domain.outing.exception.QrExpiredException
import java.time.Instant

object QrExpValidator {
    fun validate(exp: Long) {
        val expMillis = if (exp < 10_000_000_000L) exp * 1000 else exp
        if (Instant.now().toEpochMilli() > expMillis) {
            throw QrExpiredException()
        }
    }
}
