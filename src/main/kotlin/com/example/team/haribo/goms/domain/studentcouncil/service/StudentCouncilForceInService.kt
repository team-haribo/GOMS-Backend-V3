package com.example.team.haribo.goms.domain.studentcouncil.service

import com.example.team.haribo.goms.domain.outing.dto.response.QrComingResponse

interface StudentCouncilForceInService {
    fun `in`(memberId: Long): QrComingResponse
}
