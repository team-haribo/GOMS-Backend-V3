package com.teamharibo.goms.domain.auth.exception

import com.teamharibo.goms.global.exception.ErrorCode
import com.teamharibo.goms.global.exception.GlobalException

class TooManyRequestsException : GlobalException(ErrorCode.TOO_MANY_REQUESTS)
