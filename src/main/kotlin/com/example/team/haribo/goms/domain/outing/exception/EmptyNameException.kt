package com.example.team.haribo.goms.domain.outing.exception

import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException

class EmptyNameException : GlobalException(ErrorCode.EMPTY_NAME)
