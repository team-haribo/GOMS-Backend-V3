package com.example.team.haribo.goms.domain.studentcouncil.controller

import com.example.team.haribo.goms.domain.report.dto.request.ReportResolveRequest
import com.example.team.haribo.goms.domain.report.dto.response.ReportListResponse
import com.example.team.haribo.goms.domain.report.dto.response.ReportResolveResponse
import com.example.team.haribo.goms.domain.report.service.StudentCouncilPendingReportListService
import com.example.team.haribo.goms.domain.report.service.StudentCouncilReportResolveService
import com.example.team.haribo.goms.domain.report.service.StudentCouncilResolvedReportListService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v3/student-council")
class StudentCouncilReportController(
    private val pendingReportListService: StudentCouncilPendingReportListService,
    private val resolvedReportListService: StudentCouncilResolvedReportListService,
    private val reportResolveService: StudentCouncilReportResolveService
) {

    @GetMapping("/report/pending")
    fun pendingReportList(): ResponseEntity<ReportListResponse> {
        return ResponseEntity.ok(pendingReportListService.getPendingReports())
    }

    @GetMapping("/report/resolved")
    fun resolvedReportList(): ResponseEntity<ReportListResponse> {
        return ResponseEntity.ok(resolvedReportListService.getResolvedReports())
    }

    @PatchMapping("/report/{reportId}")
    fun resolveReport(
        @PathVariable reportId: Long,
        @RequestBody request: ReportResolveRequest
    ): ResponseEntity<ReportResolveResponse> {
        return ResponseEntity.ok(reportResolveService.resolve(reportId, request))
    }
}