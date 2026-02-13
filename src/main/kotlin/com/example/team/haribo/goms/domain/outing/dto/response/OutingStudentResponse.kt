package com.example.team.haribo.goms.domain.outing.dto.response

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class OutingStudentResponse(
    @JsonProperty("name")
    val name: String,

    @JsonProperty("grade")
    val grade: Long,

    @JsonProperty("department")
    val department: String,

    @JsonProperty("outing_at")
    val outingAt: LocalDateTime
)
