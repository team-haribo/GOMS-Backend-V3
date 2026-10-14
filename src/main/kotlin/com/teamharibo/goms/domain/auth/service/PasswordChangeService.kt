package com.teamharibo.goms.domain.auth.service

import com.teamharibo.goms.domain.auth.dto.request.PasswordChangeRequest

interface PasswordChangeService {
    fun changePassword(request: PasswordChangeRequest)
}
