package com.example.team.haribo.goms.domain.report.exception

import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException

class AlreadyReportedReviewException : GlobalException(ErrorCode.ALREADY_REPORTED_REVIEW)