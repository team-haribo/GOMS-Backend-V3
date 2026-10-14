package com.teamharibo.goms.domain.review.exception

import com.teamharibo.goms.global.exception.ErrorCode
import com.teamharibo.goms.global.exception.GlobalException

class NotFoundReviewException : GlobalException(ErrorCode.NOT_FOUND_REVIEW)
