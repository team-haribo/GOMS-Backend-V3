package com.teamharibo.goms.domain.studentcouncil.exception

import com.teamharibo.goms.global.exception.ErrorCode
import com.teamharibo.goms.global.exception.GlobalException

class RoleConflictException : GlobalException(ErrorCode.ROLE_CONFLICT)
