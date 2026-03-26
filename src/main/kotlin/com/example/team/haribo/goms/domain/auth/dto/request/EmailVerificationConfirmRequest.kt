package com.example.team.haribo.goms.domain.auth.dto.request

import com.example.team.haribo.goms.domain.common.enums.Purpose
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

data class EmailVerificationConfirmRequest(
    @field:NotBlank(message = "이메일은 비어 있을 수 없습니다.")
    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    val email: String,

    @field:NotBlank(message = "인증 코드는 비어 있을 수 없습니다.")
    @field:Pattern(regexp = "^\\d{6}$", message = "인증 코드는 6자리 숫자여야 합니다.")
    val code: String,

    @field:NotNull(message = "purpose 는 필수입니다.")
    val purpose: Purpose
)