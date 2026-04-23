package com.example.team.haribo.goms.domain.late.service

import com.example.team.haribo.goms.domain.late.dto.response.LateRankListResponse

interface LateRankService {
    fun getTop5(): LateRankListResponse
}
