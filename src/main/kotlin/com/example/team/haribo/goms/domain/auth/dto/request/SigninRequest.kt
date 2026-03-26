package com.example.team.haribo.goms.domain.auth.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class SigninRequest(
    @field:NotBlank(message = "이메일은 비어 있을 수 없습니다.")
    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    val email: String,

    @field:NotBlank(message = "비밀번호는 비어 있을 수 없습니다.")
    val password: String
)