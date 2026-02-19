package com.example.team.haribo.goms.domain.late.dto.response

import com.example.team.haribo.goms.domain.common.enums.Department

data class LateStudentsResponse(
    val name: String,
    val grade: Int,
    val department: Department,
)
