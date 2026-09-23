package com.teamharibo.goms.domain.review.exception

import com.teamharibo.goms.global.exception.ErrorCode
import com.teamharibo.goms.global.exception.GlobalException

class ReviewForbiddenException : GlobalException(ErrorCode.REVIEW_FORBIDDEN)
