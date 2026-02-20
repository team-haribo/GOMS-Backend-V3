package com.example.team.haribo.goms.domain.late.dto.response

import com.example.team.haribo.goms.domain.studentcouncil.dto.response.LateStudentResponse

data class LateRankListResponse(
    val students: List<LateStudentResponse>
)
