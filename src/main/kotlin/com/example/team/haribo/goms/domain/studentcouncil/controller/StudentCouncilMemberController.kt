package com.example.team.haribo.goms.domain.studentcouncil.controller

import com.example.team.haribo.goms.domain.common.enums.Department
import com.example.team.haribo.goms.domain.common.enums.Gender
import com.example.team.haribo.goms.domain.common.enums.Role
import com.example.team.haribo.goms.domain.common.enums.Status
import com.example.team.haribo.goms.domain.studentcouncil.dto.request.UpdateRoleRequest
import com.example.team.haribo.goms.domain.studentcouncil.dto.request.UpdateStatusRequest
import com.example.team.haribo.goms.domain.studentcouncil.dto.response.StudentSearchResponse
import com.example.team.haribo.goms.domain.studentcouncil.dto.response.StudentsListResponse
import com.example.team.haribo.goms.domain.studentcouncil.service.StudentCouncilMemberFilterService
import com.example.team.haribo.goms.domain.studentcouncil.service.StudentCouncilMemberListService
import com.example.team.haribo.goms.domain.studentcouncil.service.StudentCouncilMemberSearchService
import com.example.team.haribo.goms.domain.studentcouncil.service.StudentCouncilOutingAllowedService
import com.example.team.haribo.goms.domain.studentcouncil.service.StudentCouncilRoleUpdateService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Positive
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Student Council Member", description = "학생회 학생 관리 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@Validated
@RequestMapping("/api/v3/student-council")
class StudentCouncilMemberController(
    private val memberListService: StudentCouncilMemberListService,
    private val memberSearchService: StudentCouncilMemberSearchService,
    private val memberFilterService: StudentCouncilMemberFilterService,
    private val roleUpdateService: StudentCouncilRoleUpdateService,
    private val outingAllowedService: StudentCouncilOutingAllowedService
) {

    @Operation(
        summary = "전체 학생 목록 조회",
        description = "학생회에서 전체 학생 목록을 조회합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "조회 성공")
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
                description = "학생 이름",
                required = false,
                example = "김의준"
            )
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "검색 성공")
        ]
    )
    @GetMapping("/search")
    fun searchMembers(
        @RequestParam("name", required = false) name: String?
    ): ResponseEntity<StudentSearchResponse> {
        return ResponseEntity.ok(memberSearchService.search(name))
    }

    @Operation(
        summary = "학생 필터 조회",
        description = "이름, 학년, 학과, 성별, 상태, 권한 기준으로 학생 목록을 필터링합니다.",
        parameters = [
            Parameter(name = "name", description = "학생 이름", required = false, example = "김의준"),
            Parameter(name = "grade", description = "학년", required = false, example = "2"),
            Parameter(name = "department", description = "학과", required = false, example = "SW"),
            Parameter(name = "gender", description = "성별", required = false, example = "MALE"),
            Parameter(name = "status", description = "상태", required = false, example = "COMING"),
            Parameter(name = "role", description = "권한", required = false, example = "ROLE_STUDENT")
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "조회 성공")
        ]
    )
    @GetMapping("/filter")
    fun filterMembers(
        @RequestParam("name", required = false) name: String?,
        @RequestParam("grade", required = false)
        @Min(value = 1, message = "grade 는 1 이상이어야 합니다.")
        @Max(value = 3, message = "grade 는 3 이하여야 합니다.")
        grade: Int?,
        @RequestParam("department", required = false) department: Department?,
        @RequestParam("gender", required = false) gender: Gender?,
        @RequestParam("status", required = false) status: Status?,
        @RequestParam("role", required = false) role: Role?
    ): ResponseEntity<StudentsListResponse> {
        return ResponseEntity.ok(
            memberFilterService.filter(
                name = name,
                grade = grade,
                department = department,
                gender = gender,
                status = status,
                role = role
            )
        )
    }

    @Operation(
        summary = "학생 권한 변경",
        description = "특정 학생의 권한을 변경합니다.",
        parameters = [
            Parameter(
                name = "memberId",
                description = "학생 ID",
                required = true,
                example = "1"
            )
        ],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [
                Content(
                    schema = Schema(implementation = UpdateRoleRequest::class)
                )
            ]
        ),
        responses = [
            ApiResponse(responseCode = "200", description = "권한 변경 성공")
        ]
    )
    @PatchMapping("/role/{memberId}")
    fun updateRole(
        @PathVariable
        @Positive(message = "memberId 는 1 이상이어야 합니다.")
        memberId: Long,
        @Valid @RequestBody request: UpdateRoleRequest
    ): ResponseEntity<Void> {
        roleUpdateService.update(memberId, request.role)
        return ResponseEntity.ok().build()
    }

    @Operation(
        summary = "외출 허용 상태 변경",
        description = "특정 학생의 외출 허용 상태를 변경합니다.",
        parameters = [
            Parameter(
                name = "memberId",
                description = "학생 ID",
                required = true,
                example = "1"
            )
        ],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [
                Content(
                    schema = Schema(implementation = UpdateStatusRequest::class)
                )
            ]
        ),
        responses = [
            ApiResponse(responseCode = "200", description = "상태 변경 성공")
        ]
    )
    @PatchMapping("/outing-allowed/{memberId}")
    fun updateOutingAllowed(
        @PathVariable
        @Positive(message = "memberId 는 1 이상이어야 합니다.")
        memberId: Long,
        @Valid @RequestBody request: UpdateStatusRequest
    ): ResponseEntity<Void> {
        outingAllowedService.update(memberId, request.status)
        return ResponseEntity.ok().build()
    }
}