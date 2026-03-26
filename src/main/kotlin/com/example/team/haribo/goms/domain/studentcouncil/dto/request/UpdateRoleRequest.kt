package com.example.team.haribo.goms.domain.studentcouncil.dto.request

import com.example.team.haribo.goms.domain.common.enums.Role
import jakarta.validation.constraints.NotNull

data class UpdateRoleRequest(
    @field:NotNull(message = "role 은 필수입니다.")
    val role: Role
)