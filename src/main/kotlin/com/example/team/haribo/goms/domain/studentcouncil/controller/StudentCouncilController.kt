package com.example.team.haribo.goms.domain.studentcouncil.controller

import com.example.team.haribo.goms.domain.studentcouncil.dto.response.QrIssueResponse
import com.example.team.haribo.goms.domain.studentcouncil.service.QrIssueService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v3/student-council")
class StudentCouncilController(
    private val qrIssueService: QrIssueService
) {

    @PreAuthorize("hasAuthority('ROLE_STUDENT_COUNCIL')")
    @PostMapping("/qr")
    fun issueQr(): ResponseEntity<QrIssueResponse> {
        return ResponseEntity.ok(qrIssueService.issue())
    }
}
