package com.example.team.haribo.goms.GomsServerV3.global.exception

class GlobalException(
    val errorCode: ErrorCode
) : RuntimeException(errorCode.message)