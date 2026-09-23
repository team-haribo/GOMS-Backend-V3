package com.teamharibo.goms.domain.s3.exception

import com.teamharibo.goms.global.exception.ErrorCode
import com.teamharibo.goms.global.exception.GlobalException

class InvalidImageTypeException : GlobalException(ErrorCode.INVALID_PROFILE_IMAGE_TYPE)