package com.example.team.haribo.goms.domain.auth.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class PasswordChangeRequest(
    @field:NotBlank(message = "이메일은 비어 있을 수 없습니다.")
    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    val email: String,

    @field:NotBlank(message = "verifiedToken 은 비어 있을 수 없습니다.")
    val verifiedToken: String,

    @field:NotBlank(message = "새 비밀번호는 비어 있을 수 없습니다.")
    @field:Pattern(
        regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&?~*])[a-zA-Z0-9!@#$%^&?~*]{6,15}$",
        message = "비밀번호 형식이 올바르지 않습니다."
    )
    val newPassword: String
)