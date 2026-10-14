package com.teamharibo.goms.domain.report.exception

import com.teamharibo.goms.global.exception.ErrorCode
import com.teamharibo.goms.global.exception.GlobalException

class ReportContentTooLongException : GlobalException(ErrorCode.REPORT_CONTENT_TOO_LONG)