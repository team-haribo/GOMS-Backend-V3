package com.example.team.haribo.goms.domain.report.service

import com.example.team.haribo.goms.domain.report.dto.response.ReportListResponse

interface ReportMyListService {
    fun getMyReports(): ReportListResponse
}