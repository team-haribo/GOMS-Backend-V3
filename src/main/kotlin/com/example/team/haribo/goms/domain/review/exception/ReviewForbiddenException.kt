package com.example.team.haribo.goms.domain.review.exception

import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException

class ReviewForbiddenException : GlobalException(ErrorCode.REVIEW_FORBIDDEN)
