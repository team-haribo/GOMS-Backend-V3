package com.example.team.haribo.goms.domain.notification.controller

import com.example.team.haribo.goms.domain.notification.dto.request.DeviceTokenRegisterRequest
import com.example.team.haribo.goms.domain.notification.service.DeviceTokenDeleteService
import com.example.team.haribo.goms.domain.notification.service.DeviceTokenRegisterService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
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

@Tag(name = "Notification", description = "알림 토큰 관련 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@Validated
@RequestMapping("/api/v3/notification")
class NotificationTokenController(
    private val deviceTokenRegisterService: DeviceTokenRegisterService,
    private val deviceTokenDeleteService: DeviceTokenDeleteService
) {

    @Operation(
        summary = "디바이스 토큰 등록",
        description = "푸시 알림을 위한 디바이스 토큰을 등록하거나 갱신합니다.",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [
                Content(
                    schema = Schema(implementation = DeviceTokenRegisterRequest::class)
                )
            ]
        ),
        responses = [
            ApiResponse(responseCode = "200", description = "등록 성공"),
            ApiResponse(responseCode = "401", description = "인증 실패")
        ]
    )
    @PostMapping("/token")
    fun register(
        @Valid @RequestBody request: DeviceTokenRegisterRequest
    ): ResponseEntity<Void> {
        deviceTokenRegisterService.register(request)
        return ResponseEntity.ok().build()
    }

    @Operation(
        summary = "디바이스 토큰 삭제",
        description = "deviceId 기준으로 등록된 디바이스 토큰을 삭제합니다.",
        parameters = [
            Parameter(
                name = "deviceId",
                description = "삭제할 디바이스 ID",
                required = true,
                example = "android-13-pixel6"
            )
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "삭제 성공"),
            ApiResponse(responseCode = "401", description = "인증 실패")
        ]
    )
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