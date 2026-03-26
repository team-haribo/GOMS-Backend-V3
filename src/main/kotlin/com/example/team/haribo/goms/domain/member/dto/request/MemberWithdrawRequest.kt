package com.example.team.haribo.goms.domain.member.dto.request

import jakarta.validation.constraints.NotBlank

data class MemberWithdrawRequest(
    @field:NotBlank(message = "비밀번호는 비어 있을 수 없습니다.")
    val password: String
)