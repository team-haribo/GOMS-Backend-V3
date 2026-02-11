package com.example.team.haribo.goms.domain.auth.service

import com.example.team.haribo.goms.domain.auth.dto.request.PasswordChangeRequest

interface PasswordChangeService {
    fun changePassword(request: PasswordChangeRequest)
}
