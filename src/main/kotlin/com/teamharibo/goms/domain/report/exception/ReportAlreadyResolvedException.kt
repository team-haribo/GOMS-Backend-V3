package com.teamharibo.goms.domain.report.exception

import com.teamharibo.goms.global.exception.ErrorCode
import com.teamharibo.goms.global.exception.GlobalException

class ReportAlreadyResolvedException : GlobalException(ErrorCode.REPORT_ALREADY_RESOLVED)