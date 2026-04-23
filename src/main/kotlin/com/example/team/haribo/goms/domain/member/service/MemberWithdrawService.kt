package com.example.team.haribo.goms.domain.member.service

import com.example.team.haribo.goms.domain.member.dto.request.MemberWithdrawRequest

interface MemberWithdrawService {
    fun withdraw(request: MemberWithdrawRequest)
}