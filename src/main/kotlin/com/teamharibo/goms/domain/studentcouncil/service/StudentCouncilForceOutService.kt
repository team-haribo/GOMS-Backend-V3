package com.teamharibo.goms.domain.studentcouncil.service

import com.teamharibo.goms.domain.outing.dto.response.QrOutingResponse

interface StudentCouncilForceOutService {
    fun out(memberId: Long): QrOutingResponse
}
