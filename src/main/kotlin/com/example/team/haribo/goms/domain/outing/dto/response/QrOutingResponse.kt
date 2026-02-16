package com.example.team.haribo.goms.domain.outing.dto.response

import com.example.team.haribo.goms.domain.common.enums.Action
import com.example.team.haribo.goms.domain.common.enums.Status
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class QrOutingResponse(
    val action: Action,
    @JsonProperty("outing_id")
    val outingId: Long,
    val status: Status,
    @JsonProperty("outing_at")
    val outingAt: LocalDateTime
)
