package com.teamharibo.goms.domain.report.exception

import com.teamharibo.goms.global.exception.ErrorCode
import com.teamharibo.goms.global.exception.GlobalException

class NotFoundReportException : GlobalException(ErrorCode.NOT_FOUND_REPORT)