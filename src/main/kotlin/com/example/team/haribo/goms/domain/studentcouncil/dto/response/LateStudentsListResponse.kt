package com.example.team.haribo.goms.domain.studentcouncil.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class LateStudentsListResponse(
    @JsonProperty("students")
    val students: List<LateStudentResponse>
)
