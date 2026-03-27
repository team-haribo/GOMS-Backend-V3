package com.example.team.haribo.goms.domain.outing.dto.response

import java.time.LocalDateTime

data class OutingStudentResponse(
    val name: String,
    val grade: Long,
    val department: String,
    val outingAt: LocalDateTime
)