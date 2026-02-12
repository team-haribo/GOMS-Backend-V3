package com.example.team.haribo.goms.domain.auth.service

import com.example.team.haribo.goms.domain.auth.dto.request.SigninRequest
import com.example.team.haribo.goms.domain.auth.dto.response.TokenResponse

interface SigninService {
    fun signin(request: SigninRequest): TokenResponse
}
