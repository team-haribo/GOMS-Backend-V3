package com.example.team.haribo.goms.domain.studentcouncil.dto.response

import com.example.team.haribo.goms.domain.common.enums.Department
import com.example.team.haribo.goms.domain.common.enums.Role
import com.example.team.haribo.goms.domain.common.enums.Status
import java.time.LocalDateTime

data class LateStudentResponse(
    val memberId: Long,
    val name: String,
    val grade: Int,
    val department: Department,
    val role: Role,
    val status: Status,
    val comingAt: LocalDateTime
)