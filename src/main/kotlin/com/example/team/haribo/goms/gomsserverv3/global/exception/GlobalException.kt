package com.example.team.haribo.goms.gomsserverv3.global.exception

class GlobalException(
    val errorCode: ErrorCode
) : RuntimeException(errorCode.message)