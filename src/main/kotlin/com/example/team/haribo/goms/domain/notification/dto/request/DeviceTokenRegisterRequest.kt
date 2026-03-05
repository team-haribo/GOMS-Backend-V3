package com.example.team.haribo.goms.domain.notification.dto.request

import com.example.team.haribo.goms.domain.common.enums.Platform
import com.fasterxml.jackson.annotation.JsonProperty

data class DeviceTokenRegisterRequest(

    @JsonProperty("fcm_token")
    val fcmToken: String,

    val platform: Platform,

    @JsonProperty("device_id")
    val deviceId: String
)