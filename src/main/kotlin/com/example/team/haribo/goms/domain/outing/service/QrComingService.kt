package com.example.team.haribo.goms.domain.outing.service

import com.example.team.haribo.goms.domain.outing.dto.request.QrToggleRequest
import com.example.team.haribo.goms.domain.outing.dto.response.QrComingResponse

interface QrComingService {
    fun coming(request: QrToggleRequest): QrComingResponse
}
