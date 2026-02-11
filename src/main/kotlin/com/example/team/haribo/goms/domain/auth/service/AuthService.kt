package com.example.team.haribo.goms.domain.auth.service

import com.example.team.haribo.goms.domain.auth.dto.request.*
import com.example.team.haribo.goms.domain.auth.dto.response.TokenResponse

interface AuthService {

    fun signup(request: SignupRequest)

    fun signin(request: SigninRequest): TokenResponse

    fun reissue(refreshTokenHeader: String): TokenResponse

    fun changePassword(request: PasswordChangeRequest)

    fun signout(refreshTokenHeader: String)
}
