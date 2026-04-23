package com.example.team.haribo.goms.domain.review.exception

import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException

class AlreadyReviewedPlaceException : GlobalException(ErrorCode.ALREADY_REVIEWED_PLACE)
