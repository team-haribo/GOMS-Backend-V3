package com.example.team.haribo.goms.domain.notification.service

import com.example.team.haribo.goms.domain.notification.dto.request.DeviceTokenRegisterRequest

interface DeviceTokenRegisterService {

    fun register(request: DeviceTokenRegisterRequest)
}