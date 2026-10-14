package com.teamharibo.goms.domain.place.exception

import com.teamharibo.goms.global.exception.ErrorCode
import com.teamharibo.goms.global.exception.GlobalException

class InactivePlaceException : GlobalException(ErrorCode.INACTIVE_PLACE)