package com.example.team.haribo.goms.domain.auth.service

import com.example.team.haribo.goms.domain.auth.dto.request.SignupRequest

interface SignupService {
    fun signup(request: SignupRequest)
}
