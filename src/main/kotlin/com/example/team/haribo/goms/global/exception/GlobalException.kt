package com.example.team.haribo.goms.global.exception

class GlobalException(
    val errorCode: ErrorCode
) : RuntimeException(errorCode.message)