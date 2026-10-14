package com.teamharibo.goms.domain.studentcouncil.dto.response

import com.teamharibo.goms.domain.common.enums.Department
import com.teamharibo.goms.domain.common.enums.Role
import com.teamharibo.goms.domain.common.enums.Status

data class StudentResponse(
    val memberId: Long,
    val name: String,
    val grade: Int,
    val department: Department,
    val role: Role,
    val status: Status,
    val profileImageUrl: String?
)