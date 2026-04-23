package com.example.team.haribo.goms.domain.auth.dto.request

import com.example.team.haribo.goms.domain.common.enums.Department
import com.example.team.haribo.goms.domain.common.enums.Gender
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class SignupRequest(
    @field:NotBlank(message = "이메일은 비어 있을 수 없습니다.")
    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    val email: String,

    @field:NotBlank(message = "verifiedToken 은 비어 있을 수 없습니다.")
    val verifiedToken: String,

    @field:NotBlank(message = "비밀번호는 비어 있을 수 없습니다.")
    @field:Pattern(
        regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&?~])[a-zA-Z0-9!@#$%^&?~]{6,15}$",
        message = "비밀번호 형식이 올바르지 않습니다."
    )
    val password: String,

    @field:NotBlank(message = "이름은 비어 있을 수 없습니다.")
    @field:Size(max = 20, message = "이름은 20자 이하여야 합니다.")
    val name: String,

    @field:NotNull(message = "기수는 필수입니다.")
    val grade: Long,

    @field:NotNull(message = "학과는 필수입니다.")
    val department: Department,

    @field:NotNull(message = "성별은 필수입니다.")
    val gender: Gender
)