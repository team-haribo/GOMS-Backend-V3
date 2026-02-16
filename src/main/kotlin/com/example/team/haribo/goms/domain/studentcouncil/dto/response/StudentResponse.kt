package com.example.team.haribo.goms.domain.studentcouncil.dto.response

import com.example.team.haribo.goms.domain.common.enums.Department
import com.fasterxml.jackson.annotation.JsonProperty

data class StudentResponse(
    @JsonProperty("member_id")
    val memberId: Long,
    val name: String,
    val grade: Int,
    val department: Department
)
