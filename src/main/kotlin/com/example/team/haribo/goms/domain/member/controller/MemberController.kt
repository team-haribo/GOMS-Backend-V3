package com.example.team.haribo.goms.domain.member.controller

import com.example.team.haribo.goms.domain.member.dto.request.MemberWithdrawRequest
import com.example.team.haribo.goms.domain.member.service.MemberWithdrawService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Member", description = "회원 관련 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@Validated
@RequestMapping("/api/v3/member")
class MemberController(
    private val memberWithdrawService: MemberWithdrawService
) {

    @Operation(
        summary = "회원 탈퇴",
        description = "비밀번호 확인 후 회원 탈퇴를 진행합니다.",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [
                Content(
                    schema = Schema(implementation = MemberWithdrawRequest::class)
                )
            ]
        ),
        responses = [
            ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "401", description = "인증 실패")
        ]
    )
    @DeleteMapping("/withdraw")
    fun withdraw(
        @Valid @RequestBody request: MemberWithdrawRequest
    ): ResponseEntity<Void> {
        memberWithdrawService.withdraw(request)
        return ResponseEntity.ok().build()
    }
}