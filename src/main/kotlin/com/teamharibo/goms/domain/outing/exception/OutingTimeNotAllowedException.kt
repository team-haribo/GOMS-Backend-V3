package com.teamharibo.goms.domain.outing.exception

import com.teamharibo.goms.global.exception.ErrorCode
import com.teamharibo.goms.global.exception.GlobalException

class OutingTimeNotAllowedException : GlobalException(ErrorCode.OUTING_TIME_NOT_ALLOWED)