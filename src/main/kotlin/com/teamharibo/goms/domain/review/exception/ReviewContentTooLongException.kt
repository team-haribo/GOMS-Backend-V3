package com.teamharibo.goms.domain.review.exception

import com.teamharibo.goms.global.exception.ErrorCode
import com.teamharibo.goms.global.exception.GlobalException

class ReviewContentTooLongException : GlobalException(ErrorCode.REVIEW_CONTENT_TOO_LONG)
