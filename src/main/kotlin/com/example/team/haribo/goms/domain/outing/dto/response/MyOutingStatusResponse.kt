package com.example.team.haribo.goms.domain.outing.dto.response

import com.example.team.haribo.goms.domain.common.enums.Status
import com.fasterxml.jackson.annotation.JsonProperty

data class MyOutingStatusResponse(
    @JsonProperty("member_id")
    val memberId: Long,

    @JsonProperty("status")
    val status: Status,

    @JsonProperty("name")
    val name: String,

    @JsonProperty("grade")
    val grade: Int,

    @JsonProperty("department")
    val department: String
)
