package com.example.team.haribo.goms.domain.studentcouncil.controller

import com.example.team.haribo.goms.domain.common.enums.Department
import com.example.team.haribo.goms.domain.common.enums.Role
import com.example.team.haribo.goms.domain.common.enums.Status
import com.example.team.haribo.goms.domain.outing.dto.response.QrComingResponse
import com.example.team.haribo.goms.domain.outing.dto.response.QrOutingResponse
import com.example.team.haribo.goms.domain.report.dto.request.ReportResolveRequest
import com.example.team.haribo.goms.domain.report.dto.response.ReportDetailResponse
import com.example.team.haribo.goms.domain.report.dto.response.ReportListResponse
import com.example.team.haribo.goms.domain.report.dto.response.ReportResolveResponse
import com.example.team.haribo.goms.domain.report.service.StudentCouncilPendingReportListService
import com.example.team.haribo.goms.domain.report.service.StudentCouncilReportResolveService
import com.example.team.haribo.goms.domain.report.service.StudentCouncilResolvedReportListService
import com.example.team.haribo.goms.domain.studentcouncil.dto.request.UpdateRoleRequest
import com.example.team.haribo.goms.domain.studentcouncil.dto.request.UpdateStatusRequest
import com.example.team.haribo.goms.domain.studentcouncil.dto.response.LateStudentsListResponse
import com.example.team.haribo.goms.domain.studentcouncil.dto.response.QrIssueResponse
import com.example.team.haribo.goms.domain.studentcouncil.dto.response.StudentSearchResponse
import com.example.team.haribo.goms.domain.studentcouncil.dto.response.StudentsListResponse
import com.example.team.haribo.goms.domain.studentcouncil.service.QrIssueService
import com.example.team.haribo.goms.domain.studentcouncil.service.StudentCouncilForceInService
import com.example.team.haribo.goms.domain.studentcouncil.service.StudentCouncilForceOutService
import com.example.team.haribo.goms.domain.studentcouncil.service.StudentCouncilLateListService
import com.example.team.haribo.goms.domain.studentcouncil.service.StudentCouncilMemberFilterService
import com.example.team.haribo.goms.domain.studentcouncil.service.StudentCouncilMemberListService
import com.example.team.haribo.goms.domain.studentcouncil.service.StudentCouncilMemberSearchService
import com.example.team.haribo.goms.domain.studentcouncil.service.StudentCouncilOutingAllowedService
import com.example.team.haribo.goms.domain.studentcouncil.service.StudentCouncilRoleUpdateService
import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/v3/student-council")
class StudentCouncilController(
    private val qrIssueService: QrIssueService,
    private val memberListService: StudentCouncilMemberListService,
    private val memberSearchService: StudentCouncilMemberSearchService,
    private val memberFilterService: StudentCouncilMemberFilterService,
    private val roleUpdateService: StudentCouncilRoleUpdateService,
    private val outingAllowedService: StudentCouncilOutingAllowedService,
    private val forceOutService: StudentCouncilForceOutService,
    private val forceInService: StudentCouncilForceInService,
    private val lateListService: StudentCouncilLateListService,
    private val pendingReportListService: StudentCouncilPendingReportListService,
    private val resolvedReportListService: StudentCouncilResolvedReportListService,
    private val reportResolveService: StudentCouncilReportResolveService
) {

    @PostMapping("/qr")
    fun issueQr(): ResponseEntity<QrIssueResponse> {
        return ResponseEntity.ok(qrIssueService.issue())
    }

    @GetMapping("/member")
    fun listMembers(): ResponseEntity<StudentsListResponse> {
        return ResponseEntity.ok(memberListService.list())
    }

    @GetMapping("/search")
    fun searchMembers(@RequestParam("name", required = false) name: String?): ResponseEntity<StudentSearchResponse> {
        return ResponseEntity.ok(memberSearchService.search(name))
    }

    @GetMapping("/filter")
    fun filterMembers(
        @RequestParam("name", required = false) name: String?,
        @RequestParam("grade", required = false) grade: Int?,
        @RequestParam("department", required = false) department: String?,
        @RequestParam("status", required = false) status: String?,
        @RequestParam("role", required = false) role: String?
    ): ResponseEntity<StudentsListResponse> {
        return ResponseEntity.ok(
            memberFilterService.filter(
                name = name,
                grade = grade,
                department = department?.let { parseDepartment(it) },
                status = status?.let { parseStatus(it) },
                role = role?.let { parseRole(it) }
            )
        )
    }

    @PatchMapping("/role/{memberId}")
    fun updateRole(
        @PathVariable memberId: Long,
        @RequestBody request: UpdateRoleRequest
    ): ResponseEntity<Void> {
        roleUpdateService.update(memberId, request.role)
        return ResponseEntity.ok().build()
    }

    @PatchMapping("/outing-allowed/{memberId}")
    fun updateOutingAllowed(
        @PathVariable memberId: Long,
        @RequestBody request: UpdateStatusRequest
    ): ResponseEntity<Void> {
        outingAllowedService.update(memberId, request.status)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/status/out/{memberId}")
    fun forceOut(@PathVariable memberId: Long): ResponseEntity<QrOutingResponse> {
        return ResponseEntity.ok(forceOutService.out(memberId))
    }

    @PostMapping("/status/in/{memberId}")
    fun forceIn(@PathVariable memberId: Long): ResponseEntity<QrComingResponse> {
        return ResponseEntity.ok(forceInService.`in`(memberId))
    }

    @GetMapping("/late")
    fun listLate(@RequestParam("date", required = false) date: String?): ResponseEntity<LateStudentsListResponse> {
        val parsed = date?.let { parseDate(it) }
        return ResponseEntity.ok(lateListService.list(parsed))
    }

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

    private fun parseDepartment(value: String): Department {
        return runCatching { Department.valueOf(value.trim().uppercase()) }
            .getOrElse { throw GlobalException(ErrorCode.INVALID_REQUEST) }
    }

    private fun parseStatus(value: String): Status {
        return runCatching { Status.valueOf(value.trim().uppercase()) }
            .getOrElse { throw GlobalException(ErrorCode.INVALID_REQUEST) }
    }

    private fun parseRole(value: String): Role {
        return runCatching { Role.valueOf(value.trim().uppercase()) }
            .getOrElse { throw GlobalException(ErrorCode.INVALID_REQUEST) }
    }

    private fun parseDate(value: String): LocalDate {
        return runCatching { LocalDate.parse(value.trim()) }
            .getOrElse { throw GlobalException(ErrorCode.INVALID_REQUEST) }
    }
}