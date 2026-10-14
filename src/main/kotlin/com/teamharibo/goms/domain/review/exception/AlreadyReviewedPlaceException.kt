package com.teamharibo.goms.domain.review.exception

import com.teamharibo.goms.global.exception.ErrorCode
import com.teamharibo.goms.global.exception.GlobalException

class AlreadyReviewedPlaceException : GlobalException(ErrorCode.ALREADY_REVIEWED_PLACE)
