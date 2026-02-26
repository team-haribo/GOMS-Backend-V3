package com.example.team.haribo.goms.domain.report.dto.response

import com.example.team.haribo.goms.domain.common.enums.ReportStatus
import java.time.LocalDateTime

data class ReportResolveResponse(
    val report_id: Long,
    val review_id: Long,
    val report_status: ReportStatus,
    val resolved_at: LocalDateTime,
    val resolved_by: Long
)