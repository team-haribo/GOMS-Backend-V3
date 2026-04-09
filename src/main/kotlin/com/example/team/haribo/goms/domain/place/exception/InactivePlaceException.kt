package com.example.team.haribo.goms.domain.place.exception

import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException

class InactivePlaceException : GlobalException(ErrorCode.INACTIVE_PLACE)