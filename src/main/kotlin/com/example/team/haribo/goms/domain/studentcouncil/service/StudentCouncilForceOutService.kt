package com.example.team.haribo.goms.domain.studentcouncil.service

import com.example.team.haribo.goms.domain.outing.dto.response.QrOutingResponse

interface StudentCouncilForceOutService {
    fun out(memberId: Long): QrOutingResponse
}
