package com.teamharibo.goms.domain.auth.service

import com.teamharibo.goms.domain.auth.dto.request.EmailVerificationConfirmRequest
import com.teamharibo.goms.domain.auth.dto.request.EmailVerificationSendRequest
import com.teamharibo.goms.domain.auth.dto.response.EmailVerificationConfirmResponse

interface EmailVerificationService {

    fun send(request: EmailVerificationSendRequest)

    fun confirm(request: EmailVerificationConfirmRequest): EmailVerificationConfirmResponse
}
