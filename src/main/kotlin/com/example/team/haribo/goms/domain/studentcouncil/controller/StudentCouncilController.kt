package com.example.team.haribo.goms.domain.studentcouncil.controller

import com.example.team.haribo.goms.domain.common.enums.Department
import com.example.team.haribo.goms.domain.common.enums.Role
import com.example.team.haribo.goms.domain.common.enums.Status
import com.example.team.haribo.goms.domain.outing.dto.response.QrComingResponse
import com.example.team.haribo.goms.domain.outing.dto.response.QrOutingResponse
import com.example.team.haribo.goms.domain.report.dto.request.ReportResolveRequest
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
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@Tag(name = "Student Council", description = "학생회 관리자 API")
@SecurityRequirement(name = "bearerAuth")
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

    @Operation(
        summary = "QR 발급",
        description = "학생회 전용 QR 토큰을 발급합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "QR 발급 성공")
        ]
    )
    @PostMapping("/qr")
    fun issueQr(): ResponseEntity<QrIssueResponse> {
        return ResponseEntity.ok(qrIssueService.issue())
    }

    @Operation(
        summary = "전체 학생 조회",
        description = "전체 학생 목록을 조회합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "학생 목록 조회 성공")
        ]
    )
    @GetMapping("/member")
    fun listMembers(): ResponseEntity<StudentsListResponse> {
        return ResponseEntity.ok(memberListService.list())
    }

    @Operation(
        summary = "학생 검색",
        description = "이름으로 학생을 검색합니다.",
        parameters = [
            Parameter(
                name = "name",
                description = "검색할 학생 이름",
                required = false,
                example = "김준표"
            )
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "학생 검색 성공")
        ]
    )
    @GetMapping("/search")
    fun searchMembers(@RequestParam("name", required = false) name: String?): ResponseEntity<StudentSearchResponse> {
        return ResponseEntity.ok(memberSearchService.search(name))
    }

    @Operation(
        summary = "학생 필터 조회",
        description = "이름, 학년, 학과, 상태, 권한 기준으로 학생을 필터링합니다.",
        parameters = [
            Parameter(name = "name", description = "학생 이름", required = false, example = "김준표"),
            Parameter(name = "grade", description = "학년", required = false, example = "2"),
            Parameter(name = "department", description = "학과", required = false, example = "SW"),
            Parameter(name = "status", description = "상태", required = false, example = "OUTING"),
            Parameter(name = "role", description = "권한", required = false, example = "ROLE_STUDENT")
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "학생 필터 조회 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 값")
        ]
    )
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

    @Operation(
        summary = "학생 권한 변경",
        description = "특정 학생의 권한을 변경합니다.",
        parameters = [
            Parameter(name = "memberId", description = "학생 ID", required = true, example = "1")
        ],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [
                Content(
                    schema = Schema(implementation = UpdateRoleRequest::class),
                    examples = [
                        ExampleObject(
                            value = """
                            {
                              "role": "ROLE_STUDENT_COUNCIL"
                            }
                            """
                        )
                    ]
                )
            ]
        ),
        responses = [
            ApiResponse(responseCode = "200", description = "권한 변경 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 값")
        ]
    )
    @PatchMapping("/role/{memberId}")
    fun updateRole(
        @PathVariable memberId: Long,
        @RequestBody request: UpdateRoleRequest
    ): ResponseEntity<Void> {
        roleUpdateService.update(memberId, request.role)
        return ResponseEntity.ok().build()
    }

    @Operation(
        summary = "외출 가능 상태 변경",
        description = "특정 학생의 외출 가능 상태를 변경합니다.",
        parameters = [
            Parameter(name = "memberId", description = "학생 ID", required = true, example = "1")
        ],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [
                Content(
                    schema = Schema(implementation = UpdateStatusRequest::class),
                    examples = [
                        ExampleObject(
                            value = """
                            {
                              "status": "ALLOWED"
                            }
                            """
                        )
                    ]
                )
            ]
        ),
        responses = [
            ApiResponse(responseCode = "200", description = "외출 가능 상태 변경 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 값")
        ]
    )
    @PatchMapping("/outing-allowed/{memberId}")
    fun updateOutingAllowed(
        @PathVariable memberId: Long,
        @RequestBody request: UpdateStatusRequest
    ): ResponseEntity<Void> {
        outingAllowedService.update(memberId, request.status)
        return ResponseEntity.ok().build()
    }

    @Operation(
        summary = "강제 외출 처리",
        description = "특정 학생을 강제로 외출 처리합니다.",
        parameters = [
            Parameter(name = "memberId", description = "학생 ID", required = true, example = "1")
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "강제 외출 처리 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 값")
        ]
    )
    @PostMapping("/status/out/{memberId}")
    fun forceOut(@PathVariable memberId: Long): ResponseEntity<QrOutingResponse> {
        return ResponseEntity.ok(forceOutService.out(memberId))
    }

    @Operation(
        summary = "강제 복귀 처리",
        description = "특정 학생을 강제로 복귀 처리합니다.",
        parameters = [
            Parameter(name = "memberId", description = "학생 ID", required = true, example = "1")
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "강제 복귀 처리 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 값")
        ]
    )
    @PostMapping("/status/in/{memberId}")
    fun forceIn(@PathVariable memberId: Long): ResponseEntity<QrComingResponse> {
        return ResponseEntity.ok(forceInService.`in`(memberId))
    }

    @Operation(
        summary = "지각자 목록 조회",
        description = "특정 날짜 기준 지각자 목록을 조회합니다. date가 없으면 기본 날짜 기준으로 조회합니다.",
        parameters = [
            Parameter(name = "date", description = "조회 날짜", required = false, example = "2026-03-09")
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "지각자 목록 조회 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 날짜 형식")
        ]
    )
    @GetMapping("/late")
    fun listLate(@RequestParam("date", required = false) date: String?): ResponseEntity<LateStudentsListResponse> {
        val parsed = date?.let { parseDate(it) }
        return ResponseEntity.ok(lateListService.list(parsed))
    }

    @Operation(
        summary = "미처리 신고 목록 조회",
        description = "처리 전 신고 목록을 조회합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "미처리 신고 목록 조회 성공")
        ]
    )
    @GetMapping("/report/pending")
    fun pendingReportList(): ResponseEntity<ReportListResponse> {
        return ResponseEntity.ok(pendingReportListService.getPendingReports())
    }

    @Operation(
        summary = "처리 완료 신고 목록 조회",
        description = "처리 완료된 신고 목록을 조회합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "처리 완료 신고 목록 조회 성공")
        ]
    )
    @GetMapping("/report/resolved")
    fun resolvedReportList(): ResponseEntity<ReportListResponse> {
        return ResponseEntity.ok(resolvedReportListService.getResolvedReports())
    }

    @Operation(
        summary = "신고 처리",
        description = "특정 신고를 승인 또는 반려 처리합니다.",
        parameters = [
            Parameter(name = "reportId", description = "신고 ID", required = true, example = "1")
        ],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [
                Content(
                    schema = Schema(implementation = ReportResolveRequest::class),
                    examples = [
                        ExampleObject(
                            value = """
                            {
                              "reportStatus": "APPROVED"
                            }
                            """
                        )
                    ]
                )
            ]
        ),
        responses = [
            ApiResponse(responseCode = "200", description = "신고 처리 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 값")
        ]
    )
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