package com.example.team.haribo.goms.domain.auth.service

import com.example.team.haribo.goms.domain.auth.dto.response.TokenResponse

interface ReissueService {
    fun reissue(refreshTokenHeader: String): TokenResponse
}
