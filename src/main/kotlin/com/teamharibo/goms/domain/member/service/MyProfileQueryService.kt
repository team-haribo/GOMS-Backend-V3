package com.teamharibo.goms.domain.member.service

import com.teamharibo.goms.domain.member.dto.response.MyProfileResponse

interface MyProfileQueryService {
    fun execute(): MyProfileResponse
}