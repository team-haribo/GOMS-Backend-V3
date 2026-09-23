package com.teamharibo.goms.domain.outing.service

import com.teamharibo.goms.domain.outing.dto.request.QrToggleRequest
import com.teamharibo.goms.domain.outing.dto.response.QrComingResponse

interface QrComingService {
    fun coming(request: QrToggleRequest): QrComingResponse
}
