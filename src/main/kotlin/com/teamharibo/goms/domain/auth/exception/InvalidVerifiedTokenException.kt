package com.teamharibo.goms.domain.auth.exception

import com.teamharibo.goms.global.exception.ErrorCode
import com.teamharibo.goms.global.exception.GlobalException

class InvalidVerifiedTokenException : GlobalException(ErrorCode.INVALID_VERIFIED_TOKEN)
