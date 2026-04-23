package com.example.team.haribo.goms.domain.studentcouncil.dto.request

import com.example.team.haribo.goms.domain.common.enums.Status
import jakarta.validation.constraints.NotNull

data class UpdateStatusRequest(
    @field:NotNull(message = "status 는 필수입니다.")
    val status: Status
)