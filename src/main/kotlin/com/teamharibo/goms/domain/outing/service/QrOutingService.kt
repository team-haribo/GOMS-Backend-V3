package com.teamharibo.goms.domain.outing.service

import com.teamharibo.goms.domain.outing.dto.request.QrToggleRequest
import com.teamharibo.goms.domain.outing.dto.response.QrOutingResponse

interface QrOutingService {
    fun outing(request: QrToggleRequest): QrOutingResponse
}
