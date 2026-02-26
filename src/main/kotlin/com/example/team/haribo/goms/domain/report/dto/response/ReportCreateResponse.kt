package com.example.team.haribo.goms.domain.report.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class ReportCreateResponse(

    @JsonProperty("report_id")
    val reportId: Long
)