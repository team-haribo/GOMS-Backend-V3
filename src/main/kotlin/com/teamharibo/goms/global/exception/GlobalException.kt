package com.teamharibo.goms.global.exception

open class GlobalException(
    val errorCode: ErrorCode
) : RuntimeException()