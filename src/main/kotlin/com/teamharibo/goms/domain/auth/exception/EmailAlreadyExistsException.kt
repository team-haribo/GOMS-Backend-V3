package com.teamharibo.goms.domain.auth.exception

import com.teamharibo.goms.global.exception.ErrorCode
import com.teamharibo.goms.global.exception.GlobalException

class EmailAlreadyExistsException : GlobalException(ErrorCode.ALREADY_REGISTERED_EMAIL)
