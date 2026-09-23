package com.teamharibo.goms.domain.late.dto.response

import com.teamharibo.goms.domain.studentcouncil.dto.response.LateStudentResponse

data class LateRankListResponse(
    val students: List<LateStudentResponse>
)
