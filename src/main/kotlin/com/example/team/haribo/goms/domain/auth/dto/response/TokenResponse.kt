package com.example.team.haribo.goms.domain.auth.dto.response

import java.time.LocalDateTime

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiresIn: LocalDateTime,
    val refreshTokenExpiresIn: LocalDateTime
)
