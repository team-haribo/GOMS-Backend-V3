package com.teamharibo.goms.domain.outing.service

import com.teamharibo.goms.domain.outing.dto.response.OutingCountResponse

interface OutingCountService {
    fun getCount(): OutingCountResponse
}
