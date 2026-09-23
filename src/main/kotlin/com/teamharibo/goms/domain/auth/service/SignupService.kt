package com.teamharibo.goms.domain.auth.service

import com.teamharibo.goms.domain.auth.dto.request.SignupRequest

interface SignupService {
    fun signup(request: SignupRequest)
}
