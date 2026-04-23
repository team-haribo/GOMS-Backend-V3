package com.example.team.haribo.goms.domain.report.exception

import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException

class NotFoundReportException : GlobalException(ErrorCode.NOT_FOUND_REPORT)