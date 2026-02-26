package com.example.team.haribo.goms.domain.report.dto.request

import com.example.team.haribo.goms.domain.common.enums.ReportStatus

data class ReportResolveRequest(
    val report_status: ReportStatus
)