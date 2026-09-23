package com.teamharibo.goms.domain.member.service

import com.teamharibo.goms.domain.member.dto.response.MyRoleResponse

interface MyRoleQueryService {
    fun execute(): MyRoleResponse
}