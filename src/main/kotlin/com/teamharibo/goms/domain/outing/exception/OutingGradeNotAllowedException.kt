package com.teamharibo.goms.domain.outing.exception

import com.teamharibo.goms.global.exception.ErrorCode
import com.teamharibo.goms.global.exception.GlobalException

class OutingGradeNotAllowedException : GlobalException(ErrorCode.OUTING_GRADE_NOT_ALLOWED)