package com.example.team.haribo.goms.domain.studentcouncil.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class StudentSearchResponse(
    @JsonProperty("students")
    val students: List<StudentResponse>
)
