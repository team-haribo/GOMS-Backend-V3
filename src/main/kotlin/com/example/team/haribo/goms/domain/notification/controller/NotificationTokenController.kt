package com.example.team.haribo.goms.domain.notification.controller

import com.example.team.haribo.goms.domain.notification.dto.request.DeviceTokenRegisterRequest
import com.example.team.haribo.goms.domain.notification.service.impl.DeviceTokenDeleteServiceImpl
import com.example.team.haribo.goms.domain.notification.service.impl.DeviceTokenRegisterServiceImpl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v3/notification")
class NotificationTokenController(
    private val deviceTokenRegisterService: DeviceTokenRegisterServiceImpl,
    private val deviceTokenDeleteService: DeviceTokenDeleteServiceImpl
) {

    @PostMapping("/token")
    fun register(@RequestBody request: DeviceTokenRegisterRequest): ResponseEntity<Void> {
        deviceTokenRegisterService.register(request)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/token/{deviceId}")
    fun delete(@PathVariable deviceId: String): ResponseEntity<Void> {
        deviceTokenDeleteService.delete(deviceId)
        return ResponseEntity.ok().build()
    }
}