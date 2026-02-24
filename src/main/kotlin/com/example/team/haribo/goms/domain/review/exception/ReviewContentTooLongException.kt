package com.example.team.haribo.goms.domain.review.exception

import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException

class ReviewContentTooLongException : GlobalException(ErrorCode.REVIEW_CONTENT_TOO_LONG)
