package com.example.team.haribo.goms.domain.outing.service

import com.example.team.haribo.goms.domain.outing.dto.response.OutingCountResponse

interface OutingCountService {
    fun getCount(): OutingCountResponse
}
