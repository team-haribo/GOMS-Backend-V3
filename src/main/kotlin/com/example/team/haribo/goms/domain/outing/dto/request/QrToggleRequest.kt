package com.example.team.haribo.goms.domain.outing.dto.request

import com.fasterxml.jackson.annotation.JsonProperty

data class QrToggleRequest(
    @JsonProperty("uuid")
    val uuid: String,

    @JsonProperty("exp")
    val exp: Long
)
