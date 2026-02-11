package com.example.team.haribo.goms.domain.auth.dto.request

import com.example.team.haribo.goms.domain.common.enums.Purpose

data class EmailVerificationSendRequest(
    val email: String,
    val purpose: Purpose
)
