package com.example.team.haribo.goms.domain.outing.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class QrToggleRequest(
    @field:NotBlank(message = "uuid 는 비어 있을 수 없습니다.")
    val uuid: String,

    @field:Positive(message = "exp 는 1 이상이어야 합니다.")
    val exp: Long
)