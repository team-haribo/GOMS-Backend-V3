package com.example.team.haribo.goms.domain.report.dto.request

import com.example.team.haribo.goms.domain.common.enums.ReportReason

data class ReportCreateRequest(
    val reason: ReportReason,
    val content: String
)