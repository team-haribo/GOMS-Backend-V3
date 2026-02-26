package com.example.team.haribo.goms.domain.report.exception

import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException

class ReportContentTooLongException : GlobalException(ErrorCode.REPORT_CONTENT_TOO_LONG)