package com.example.team.haribo.goms.domain.s3.exception

import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException

class EmptyImageException : GlobalException(ErrorCode.EMPTY_PROFILE_IMAGE)