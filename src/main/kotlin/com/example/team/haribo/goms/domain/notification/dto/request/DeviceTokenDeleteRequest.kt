package com.example.team.haribo.goms.domain.notification.dto.request

import com.fasterxml.jackson.annotation.JsonProperty

data class DeviceTokenDeleteRequest(

    @JsonProperty("device_id")
    val deviceId: String
)