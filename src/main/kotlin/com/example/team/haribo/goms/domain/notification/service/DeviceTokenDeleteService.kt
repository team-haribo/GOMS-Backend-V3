package com.example.team.haribo.goms.domain.notification.service

import com.example.team.haribo.goms.domain.notification.dto.request.DeviceTokenDeleteRequest

interface DeviceTokenDeleteService {

    fun delete(request: DeviceTokenDeleteRequest)
}