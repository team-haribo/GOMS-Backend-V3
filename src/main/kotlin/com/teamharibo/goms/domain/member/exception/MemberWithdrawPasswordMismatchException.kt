package com.teamharibo.goms.domain.member.exception

import com.teamharibo.goms.global.exception.ErrorCode
import com.teamharibo.goms.global.exception.GlobalException

class MemberWithdrawPasswordMismatchException : GlobalException(ErrorCode.MEMBER_WITHDRAW_PASSWORD_MISMATCH)