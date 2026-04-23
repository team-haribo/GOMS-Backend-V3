package com.example.team.haribo.goms.domain.outing.dto.response

import com.example.team.haribo.goms.domain.common.enums.Action
import com.example.team.haribo.goms.domain.common.enums.Status
import java.time.LocalDateTime

data class QrComingResponse(
    val action: Action,
    val outingId: Long,
    val status: Status,
    val comingAt: LocalDateTime,
    val lateCreated: Boolean,
    val lateId: Long?
)