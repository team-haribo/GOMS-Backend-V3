package com.teamharibo.goms.domain.report.service

import com.teamharibo.goms.domain.report.dto.response.ReportListResponse

interface StudentCouncilPendingReportListService {
    fun getPendingReports(): ReportListResponse
}