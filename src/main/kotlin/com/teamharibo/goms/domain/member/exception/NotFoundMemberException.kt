package com.teamharibo.goms.domain.member.exception

import com.teamharibo.goms.global.exception.ErrorCode
import com.teamharibo.goms.global.exception.GlobalException

class NotFoundMemberException : GlobalException(ErrorCode.NOT_FOUND_MEMBER)
