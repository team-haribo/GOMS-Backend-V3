package com.teamharibo.goms.domain.s3.exception

import com.teamharibo.goms.global.exception.ErrorCode
import com.teamharibo.goms.global.exception.GlobalException

class EmptyImageException : GlobalException(ErrorCode.EMPTY_PROFILE_IMAGE)