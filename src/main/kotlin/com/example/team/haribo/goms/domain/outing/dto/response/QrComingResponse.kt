package com.example.team.haribo.goms.domain.outing.dto.response

import com.example.team.haribo.goms.domain.common.enums.Action
import com.example.team.haribo.goms.domain.common.enums.Status
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class QrComingResponse(
    @JsonProperty("action")
    val action: Action,

    @JsonProperty("outing_id")
    val outingId: Long,

    @JsonProperty("status")
    val status: Status,

    @JsonProperty("coming_at")
    val comingAt: LocalDateTime,

    @JsonProperty("late_created")
    val lateCreated: Boolean,

    @JsonProperty("late_id")
    val lateId: Long?
)
