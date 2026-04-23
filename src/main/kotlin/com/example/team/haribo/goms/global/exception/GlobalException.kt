package com.example.team.haribo.goms.global.exception

open class GlobalException(
    val errorCode: ErrorCode
) : RuntimeException()