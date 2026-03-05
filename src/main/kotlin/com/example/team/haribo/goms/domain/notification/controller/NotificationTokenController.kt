package com.example.team.haribo.goms.domain.notification.controller

import com.example.team.haribo.goms.domain.notification.dto.request.DeviceTokenDeleteRequest
import com.example.team.haribo.goms.domain.notification.dto.request.DeviceTokenRegisterRequest
import com.example.team.haribo.goms.domain.notification.service.DeviceTokenDeleteService
import com.example.team.haribo.goms.domain.notification.service.DeviceTokenRegisterService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v3/notification")
class NotificationTokenController(
    private val deviceTokenRegisterService: DeviceTokenRegisterService,
    private val deviceTokenDeleteService: DeviceTokenDeleteService
) {

    @PostMapping("/token")
    fun register(
        @RequestBody request: DeviceTokenRegisterRequest
    ): ResponseEntity<Void> {
        deviceTokenRegisterService.register(request)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/token")
    fun delete(
        @RequestBody request: DeviceTokenDeleteRequest
    ): ResponseEntity<Void> {
        deviceTokenDeleteService.delete(request)
        return ResponseEntity.ok().build()
    }
}