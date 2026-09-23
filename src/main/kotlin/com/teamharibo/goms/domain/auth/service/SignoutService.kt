package com.teamharibo.goms.domain.auth.service

interface SignoutService {
    fun signout(refreshTokenHeader: String)
}
