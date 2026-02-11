package com.example.team.haribo.goms.domain.auth.service

import com.example.team.haribo.goms.domain.auth.dto.request.EmailVerificationConfirmRequest
import com.example.team.haribo.goms.domain.auth.dto.request.EmailVerificationSendRequest
import com.example.team.haribo.goms.domain.auth.dto.response.EmailVerificationConfirmResponse

interface EmailVerificationService {

    fun send(request: EmailVerificationSendRequest)

    fun confirm(request: EmailVerificationConfirmRequest): EmailVerificationConfirmResponse
}
