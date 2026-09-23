package com.teamharibo.goms.domain.outing.service

import com.teamharibo.goms.domain.outing.dto.response.MyOutingStatusResponse

interface MyOutingStatusService {
    fun getStatus(): MyOutingStatusResponse
}
