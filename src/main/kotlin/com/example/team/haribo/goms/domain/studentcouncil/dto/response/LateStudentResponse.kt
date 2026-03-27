package com.example.team.haribo.goms.domain.studentcouncil.dto.response

import com.example.team.haribo.goms.domain.common.enums.Department
import java.time.LocalDateTime

data class LateStudentResponse(
    val memberId: Long,
    val name: String,
    val grade: Int,
    val department: Department,
    val comingAt: LocalDateTime
)