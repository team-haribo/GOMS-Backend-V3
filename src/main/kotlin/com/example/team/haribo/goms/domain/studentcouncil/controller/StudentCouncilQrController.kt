package com.example.team.haribo.goms.domain.studentcouncil.controller

import com.example.team.haribo.goms.domain.studentcouncil.dto.response.QrIssueResponse
import com.example.team.haribo.goms.domain.studentcouncil.service.QrIssueService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v3/student-council")
class StudentCouncilQrController(
    private val qrIssueService: QrIssueService
) {

    @PostMapping("/qr")
    fun issueQr(): ResponseEntity<QrIssueResponse> {
        return ResponseEntity.ok(qrIssueService.issue())
    }
}