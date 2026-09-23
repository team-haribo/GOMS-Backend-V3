package com.teamharibo.goms.domain.outing.exception

import com.teamharibo.goms.global.exception.ErrorCode
import com.teamharibo.goms.global.exception.GlobalException

class AlreadyOutingException : GlobalException(ErrorCode.ALREADY_OUTING)
