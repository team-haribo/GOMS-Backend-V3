package com.example.team.haribo.goms.domain.auth.dto.request

import com.example.team.haribo.goms.domain.common.enums.Purpose
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class EmailVerificationSendRequest(
    @field:NotBlank(message = "이메일은 비어 있을 수 없습니다.")
    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    val email: String,

    @field:NotNull(message = "purpose 는 필수입니다.")
    val purpose: Purpose
)