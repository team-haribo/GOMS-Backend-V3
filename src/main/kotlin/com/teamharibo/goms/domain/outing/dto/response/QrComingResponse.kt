package com.teamharibo.goms.domain.outing.dto.response

import com.teamharibo.goms.domain.common.enums.Action
import com.teamharibo.goms.domain.common.enums.Status
import java.time.LocalDateTime

data class QrComingResponse(
    val action: Action,
    val outingId: Long,
    val status: Status,
    val comingAt: LocalDateTime,
    val lateCreated: Boolean,
    val lateId: Long?
)