package com.teamharibo.goms.domain.auth.exception

import com.teamharibo.goms.global.exception.ErrorCode
import com.teamharibo.goms.global.exception.GlobalException

class VerificationCodeExpiredException : GlobalException(ErrorCode.VERIFICATION_CODE_EXPIRED)
