package com.example.team.haribo.goms.domain.member.dto.response

import com.example.team.haribo.goms.domain.common.enums.Department
import com.example.team.haribo.goms.domain.common.enums.Gender
import com.example.team.haribo.goms.domain.common.enums.Role
import com.example.team.haribo.goms.domain.common.enums.Status

data class MyProfileResponse(
    val memberId: Long,
    val email: String,
    val name: String,
    val grade: Int,
    val department: Department,
    val gender: Gender,
    val role: Role,
    val status: Status,
    val profileImageUrl: String?
)