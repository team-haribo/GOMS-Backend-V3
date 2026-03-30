package com.example.team.haribo.goms.domain.member.service

import com.example.team.haribo.goms.domain.member.dto.response.MyRoleResponse

interface MyRoleQueryService {
    fun execute(): MyRoleResponse
}