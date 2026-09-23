package com.teamharibo.goms.domain.member.service

import com.teamharibo.goms.domain.member.dto.request.MemberWithdrawRequest

interface MemberWithdrawService {
    fun withdraw(request: MemberWithdrawRequest)
}