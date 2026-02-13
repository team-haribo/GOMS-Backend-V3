package com.example.team.haribo.goms.domain.outing.service

import com.example.team.haribo.goms.domain.outing.dto.response.MyOutingStatusResponse

interface MyOutingStatusService {
    fun getStatus(): MyOutingStatusResponse
}
