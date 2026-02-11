package com.example.team.haribo.goms.domain.auth.dto.request

data class PasswordChangeRequest(
    val email: String,
    val verifiedToken: String,
    val newPassword: String
)
