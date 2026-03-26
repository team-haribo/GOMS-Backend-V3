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
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
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
        @RequestParam("grade", required = false) grade: Int?,
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
}