package com.example.team.haribo.goms.domain.notification.exception

import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException

class NotFoundDeviceTokenException :
    GlobalException(ErrorCode.NOT_FOUND_DEVICE_TOKEN)