package com.example.team.haribo.goms.domain.outing.dto.response

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class OutingStudentResponse(
    val name: String,
    val grade: Long,
    val department: String,
    @JsonProperty("outing_at")
    val outingAt: LocalDateTime
)
