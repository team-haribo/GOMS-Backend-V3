package com.example.team.haribo.goms.domain.outing.dto.response

import com.example.team.haribo.goms.domain.common.enums.Role
import com.example.team.haribo.goms.domain.common.enums.Status
import java.time.LocalDateTime

data class OutingStudentResponse(
    val memberId: Long,
    val name: String,
    val grade: Int,
    val department: String,
    val role: Role,
    val status: Status,
    val profileImageUrl: String?,
    val outingAt: LocalDateTime
)