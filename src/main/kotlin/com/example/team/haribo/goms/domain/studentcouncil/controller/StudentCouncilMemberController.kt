package com.example.team.haribo.goms.domain.studentcouncil.controller

import com.example.team.haribo.goms.domain.common.enums.Department
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
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Positive
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

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

    @GetMapping("/member")
    fun listMembers(): ResponseEntity<StudentsListResponse> {
        return ResponseEntity.ok(memberListService.list())
    }

    @GetMapping("/search")
    fun searchMembers(
        @RequestParam("name", required = false) name: String?
    ): ResponseEntity<StudentSearchResponse> {
        return ResponseEntity.ok(memberSearchService.search(name))
    }

    @GetMapping("/filter")
    fun filterMembers(
        @RequestParam("name", required = false) name: String?,
        @RequestParam("grade", required = false)
        @Min(value = 1, message = "grade 는 1 이상이어야 합니다.")
        @Max(value = 3, message = "grade 는 3 이하여야 합니다.")
        grade: Int?,
        @RequestParam("department", required = false) department: Department?,
        @RequestParam("status", required = false) status: Status?,
        @RequestParam("role", required = false) role: Role?
    ): ResponseEntity<StudentsListResponse> {
        return ResponseEntity.ok(
            memberFilterService.filter(
                name = name,
                grade = grade,
                department = department,
                status = status,
                role = role
            )
        )
    }

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