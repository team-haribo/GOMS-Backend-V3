package com.teamharibo.goms.domain.notification.service

import com.teamharibo.goms.domain.notification.dto.request.DeviceTokenRegisterRequest

interface DeviceTokenRegisterService {
    fun register(request: DeviceTokenRegisterRequest)
}