package com.teamharibo.goms.domain.auth.service

import com.teamharibo.goms.domain.auth.dto.response.TokenResponse

interface ReissueService {
    fun reissue(refreshTokenHeader: String): TokenResponse
}
