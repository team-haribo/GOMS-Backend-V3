package com.example.team.haribo.goms.domain.report.dto.response

import com.example.team.haribo.goms.domain.common.enums.ReportStatus
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class ReportResolveResponse(

    @JsonProperty("report_id")
    val reportId: Long,

    @JsonProperty("review_id")
    val reviewId: Long,

    @JsonProperty("report_status")
    val reportStatus: ReportStatus,

    @JsonProperty("resolved_at")
    val resolvedAt: LocalDateTime,

    @JsonProperty("resolved_by")
    val resolvedBy: Long
)