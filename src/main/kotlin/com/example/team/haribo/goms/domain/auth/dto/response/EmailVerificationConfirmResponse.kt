package com.example.team.haribo.goms.domain.auth.dto.response

import java.time.LocalDateTime

data class EmailVerificationConfirmResponse(
    val verifiedToken: String,
    val verifiedTokenExpiresIn: LocalDateTime
)
