package com.example.team.haribo.goms.domain.report.dto.response

import com.example.team.haribo.goms.domain.common.enums.ReportStatus
import java.time.LocalDateTime

data class ReportResolveResponse(
    val reportId: Long,
    val reviewId: Long,
    val reportStatus: ReportStatus,
    val resolvedAt: LocalDateTime,
    val resolvedBy: Long
)