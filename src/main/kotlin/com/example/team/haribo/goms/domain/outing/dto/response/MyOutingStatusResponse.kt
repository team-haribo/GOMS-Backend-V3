package com.example.team.haribo.goms.domain.outing.dto.response

import com.example.team.haribo.goms.domain.common.enums.Status
import com.fasterxml.jackson.annotation.JsonProperty

data class MyOutingStatusResponse(
    @JsonProperty("member_id")
    val memberId: Long,
    val status: Status,
    val name: String,
    val grade: Int,
    val department: String
)
