package com.example.team.haribo.goms.domain.outing.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class OutingCountResponse(
    @JsonProperty("outing_count")
    val outingCount: Long
)
