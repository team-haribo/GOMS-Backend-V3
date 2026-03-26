package com.example.team.haribo.goms.domain.notification.dto.request

import com.example.team.haribo.goms.domain.common.enums.Platform
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class DeviceTokenRegisterRequest(

    @JsonProperty("fcm_token")
    @field:NotBlank(message = "fcmToken 은 비어 있을 수 없습니다.")
    val fcmToken: String,

    @field:NotNull(message = "platform 은 필수입니다.")
    val platform: Platform,

    @JsonProperty("device_id")
    @field:NotBlank(message = "deviceId 는 비어 있을 수 없습니다.")
    val deviceId: String
)