package com.example.team.haribo.goms.domain.member.service.impl

import com.example.team.haribo.goms.domain.member.dto.response.MyProfileResponse
import com.example.team.haribo.goms.domain.member.service.MyProfileQueryService
import com.example.team.haribo.goms.global.util.MemberUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MyProfileQueryServiceImpl(
    private val memberUtil: MemberUtil
) : MyProfileQueryService {

    @Transactional(readOnly = true)
    override fun execute(): MyProfileResponse {
        val member = memberUtil.currentMember()

        return MyProfileResponse(
            memberId = member.id!!,
            email = member.email,
            name = member.name,
            grade = member.grade,
            department = member.department,
            gender = member.gender,
            role = member.role,
            status = member.status,
            profileImageUrl = member.profileImageUrl
        )
    }
}