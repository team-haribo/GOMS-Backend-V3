package com.example.team.haribo.goms.domain.auth.dto.request

import com.example.team.haribo.goms.domain.common.enums.Purpose

data class EmailVerificationConfirmRequest(
    val email: String,
    val code: String,
    val purpose: Purpose
)
