package com.example.team.haribo.goms.domain.auth.service

interface SignoutService {
    fun signout(refreshTokenHeader: String)
}
