package com.example.team.haribo.goms.domain.member.controller

import com.example.team.haribo.goms.domain.member.dto.request.MemberWithdrawRequest
import com.example.team.haribo.goms.domain.member.service.MemberWithdrawService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v3/member")
class MemberController(
    private val memberWithdrawService: MemberWithdrawService
) {

    @DeleteMapping("/withdraw")
    fun withdraw(
        @RequestBody request: MemberWithdrawRequest
    ): ResponseEntity<Void> {
        memberWithdrawService.withdraw(request)
        return ResponseEntity.ok().build()
    }
}