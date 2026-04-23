package com.example.team.haribo.goms.domain.member.service.impl

import com.example.team.haribo.goms.domain.member.dto.response.MyRoleResponse
import com.example.team.haribo.goms.domain.member.service.MyRoleQueryService
import com.example.team.haribo.goms.global.util.MemberUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MyRoleQueryServiceImpl(
    private val memberUtil: MemberUtil
) : MyRoleQueryService {

    @Transactional(readOnly = true)
    override fun execute(): MyRoleResponse {
        val member = memberUtil.currentMember()

        return MyRoleResponse(
            memberId = member.id!!,
            email = member.email,
            name = member.name,
            role = member.role,
            profileImageUrl = member.profileImageUrl
        )
    }
}