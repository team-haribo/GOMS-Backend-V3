package com.teamharibo.goms.domain.notification.exception

import com.teamharibo.goms.global.exception.ErrorCode
import com.teamharibo.goms.global.exception.GlobalException

class NotFoundDeviceTokenException :
    GlobalException(ErrorCode.NOT_FOUND_DEVICE_TOKEN)