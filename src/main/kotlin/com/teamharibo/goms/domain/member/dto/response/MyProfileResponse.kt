package com.teamharibo.goms.domain.member.dto.response

import com.teamharibo.goms.domain.common.enums.Department
import com.teamharibo.goms.domain.common.enums.Gender
import com.teamharibo.goms.domain.common.enums.Role
import com.teamharibo.goms.domain.common.enums.Status

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