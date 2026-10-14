package com.teamharibo.goms.domain.studentcouncil.dto.request

import com.teamharibo.goms.domain.common.enums.Status
import jakarta.validation.constraints.NotNull

data class UpdateStatusRequest(
    @field:NotNull(message = "status 는 필수입니다.")
    val status: Status
)