package com.example.team.haribo.goms.domain.report.service

import com.example.team.haribo.goms.domain.report.dto.response.ReportDetailResponse

interface ReportDetailService {
    fun getReportDetail(reportId: Long): ReportDetailResponse
}