package com.example.team.haribo.goms.domain.studentcouncil.controller

import com.example.team.haribo.goms.domain.studentcouncil.dto.response.QrIssueResponse
import com.example.team.haribo.goms.domain.studentcouncil.service.QrIssueService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Student Council QR", description = "학생회 QR 발급 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v3/student-council")
class StudentCouncilQrController(
    private val qrIssueService: QrIssueService
) {

    @Operation(
        summary = "QR 발급",
        description = "외출 및 복귀 처리용 QR 정보를 발급합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "QR 발급 성공")
        ]
    )
    @PostMapping("/qr")
    fun issueQr(): ResponseEntity<QrIssueResponse> {
        return ResponseEntity.ok(qrIssueService.issue())
    }
}