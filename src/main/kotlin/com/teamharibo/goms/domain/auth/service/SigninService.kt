package com.teamharibo.goms.domain.auth.service

import com.teamharibo.goms.domain.auth.dto.request.SigninRequest
import com.teamharibo.goms.domain.auth.dto.response.TokenResponse

interface SigninService {
    fun signin(request: SigninRequest): TokenResponse
}
