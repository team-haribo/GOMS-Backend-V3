package com.teamharibo.goms.domain.late.service

import com.teamharibo.goms.domain.late.dto.response.LateRankListResponse

interface LateRankService {
    fun getTop5(): LateRankListResponse
}
