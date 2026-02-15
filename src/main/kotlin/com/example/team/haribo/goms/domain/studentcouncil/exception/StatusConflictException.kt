package com.example.team.haribo.goms.domain.studentcouncil.exception

import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException

class StatusConflictException : GlobalException(ErrorCode.STATUS_CONFLICT)
