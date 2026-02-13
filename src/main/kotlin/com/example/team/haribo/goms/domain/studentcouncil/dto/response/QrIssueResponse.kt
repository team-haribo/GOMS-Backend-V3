package com.example.team.haribo.goms.domain.studentcouncil.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class QrIssueResponse(
    @JsonProperty("uuid")
    val uuid: String,

    @JsonProperty("exp")
    val exp: Long
)