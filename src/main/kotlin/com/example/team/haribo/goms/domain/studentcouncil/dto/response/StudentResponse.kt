package com.example.team.haribo.goms.domain.studentcouncil.dto.response

import com.example.team.haribo.goms.domain.common.enums.Department
import com.example.team.haribo.goms.domain.common.enums.Role
import com.example.team.haribo.goms.domain.common.enums.Status

data class StudentResponse(
    val memberId: Long,
    val name: String,
    val grade: Int,
    val department: Department,
    val role: Role,
    val status: Status
)