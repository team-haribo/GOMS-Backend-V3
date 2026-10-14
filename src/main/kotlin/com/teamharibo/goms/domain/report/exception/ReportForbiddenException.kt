package com.teamharibo.goms.domain.report.exception

import com.teamharibo.goms.global.exception.ErrorCode
import com.teamharibo.goms.global.exception.GlobalException

class ReportForbiddenException : GlobalException(ErrorCode.REPORT_FORBIDDEN)