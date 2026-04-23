package com.example.team.haribo.goms.domain.outing.service

import com.example.team.haribo.goms.domain.outing.dto.request.QrToggleRequest
import com.example.team.haribo.goms.domain.outing.dto.response.QrOutingResponse

interface QrOutingService {
    fun outing(request: QrToggleRequest): QrOutingResponse
}
