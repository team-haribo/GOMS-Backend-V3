package com.example.team.haribo.goms.domain.notification.controller

import com.example.team.haribo.goms.domain.notification.dto.request.DeviceTokenRegisterRequest
import com.example.team.haribo.goms.domain.notification.service.DeviceTokenDeleteService
import com.example.team.haribo.goms.domain.notification.service.DeviceTokenRegisterService
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Validated
@RequestMapping("/api/v3/notification")
class NotificationTokenController(
    private val deviceTokenRegisterService: DeviceTokenRegisterService,
    private val deviceTokenDeleteService: DeviceTokenDeleteService
) {

    @PostMapping("/token")
    fun register(
        @Valid @RequestBody request: DeviceTokenRegisterRequest
    ): ResponseEntity<Void> {
        deviceTokenRegisterService.register(request)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/token/{deviceId}")
    fun delete(
        @PathVariable
        @NotBlank(message = "deviceId 는 비어 있을 수 없습니다.")
        deviceId: String
    ): ResponseEntity<Void> {
        deviceTokenDeleteService.delete(deviceId)
        return ResponseEntity.ok().build()
    }
}