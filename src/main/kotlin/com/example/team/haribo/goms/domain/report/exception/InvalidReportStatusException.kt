package com.example.team.haribo.goms.domain.report.exception

import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException

class InvalidReportStatusException : GlobalException(ErrorCode.INVALID_REPORT_STATUS)