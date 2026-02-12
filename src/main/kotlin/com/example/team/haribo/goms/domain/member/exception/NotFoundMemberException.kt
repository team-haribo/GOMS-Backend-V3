package com.example.team.haribo.goms.domain.member.exception

import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException

class NotFoundMemberException : GlobalException(ErrorCode.NOT_FOUND_MEMBER)
