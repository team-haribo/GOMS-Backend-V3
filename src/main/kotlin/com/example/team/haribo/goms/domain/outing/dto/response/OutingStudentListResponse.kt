package com.example.team.haribo.goms.domain.outing.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class OutingStudentListResponse(
    @JsonProperty("students")
    val students: List<OutingStudentResponse>
)
