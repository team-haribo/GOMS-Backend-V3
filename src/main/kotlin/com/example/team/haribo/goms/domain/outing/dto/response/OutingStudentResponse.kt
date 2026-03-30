package com.example.team.haribo.goms.domain.outing.dto.response

import java.time.LocalDateTime

data class OutingStudentResponse(
    val memberId: Long,
    val name: String,
    val grade: Int,
    val department: String,
    val outingAt: LocalDateTime
)