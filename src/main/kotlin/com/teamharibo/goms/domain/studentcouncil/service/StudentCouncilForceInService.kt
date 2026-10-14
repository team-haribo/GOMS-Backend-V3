package com.teamharibo.goms.domain.studentcouncil.service

import com.teamharibo.goms.domain.outing.dto.response.QrComingResponse

interface StudentCouncilForceInService {
    fun `in`(memberId: Long): QrComingResponse
}
