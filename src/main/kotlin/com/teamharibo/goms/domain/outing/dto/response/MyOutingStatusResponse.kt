package com.teamharibo.goms.domain.outing.dto.response

import com.teamharibo.goms.domain.common.enums.Status

data class MyOutingStatusResponse(
    val memberId: Long,
    val status: Status,
    val name: String,
    val grade: Int,
    val department: String,
    val lateCount: Long,
    val profileImageUrl: String?
)