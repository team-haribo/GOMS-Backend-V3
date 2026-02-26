package com.example.team.haribo.goms.domain.report.dto.request

import com.example.team.haribo.goms.domain.common.enums.ReportStatus
import com.fasterxml.jackson.annotation.JsonProperty

data class ReportResolveRequest(

    @JsonProperty("report_status")
    val reportStatus: ReportStatus
)