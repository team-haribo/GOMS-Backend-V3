package com.example.team.haribo.goms.domain.member.service

import com.example.team.haribo.goms.domain.member.dto.response.MyProfileResponse

interface MyProfileQueryService {
    fun execute(): MyProfileResponse
}