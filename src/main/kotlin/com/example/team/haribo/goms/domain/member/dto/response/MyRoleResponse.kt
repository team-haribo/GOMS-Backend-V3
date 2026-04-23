package com.example.team.haribo.goms.domain.member.dto.response

import com.example.team.haribo.goms.domain.common.enums.Role

data class MyRoleResponse(
    val memberId: Long,
    val email: String,
    val name: String,
    val role: Role,
    val profileImageUrl: String?
)