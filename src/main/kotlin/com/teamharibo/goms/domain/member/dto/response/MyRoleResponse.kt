package com.teamharibo.goms.domain.member.dto.response

import com.teamharibo.goms.domain.common.enums.Role

data class MyRoleResponse(
    val memberId: Long,
    val email: String,
    val name: String,
    val role: Role,
    val profileImageUrl: String?
)