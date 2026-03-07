package com.example.team.haribo.goms.domain.member.exception

import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException

class MemberWithdrawPasswordMismatchException : GlobalException(ErrorCode.MEMBER_WITHDRAW_PASSWORD_MISMATCH)