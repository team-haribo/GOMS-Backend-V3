package com.example.team.haribo.goms.domain.auth.exception

import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException

class VerificationCodeExpiredException : GlobalException(ErrorCode.VERIFICATION_CODE_EXPIRED)
