package com.teamharibo.goms.domain.report.service

import com.teamharibo.goms.domain.report.dto.response.ReportDetailResponse

interface ReportDetailService {
    fun getReportDetail(reportId: Long): ReportDetailResponse
}