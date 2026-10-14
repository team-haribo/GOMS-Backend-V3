package com.teamharibo.goms.domain.member.service.impl

import com.teamharibo.goms.domain.member.dto.response.MyProfileResponse
import com.teamharibo.goms.domain.member.service.MyProfileQueryService
import com.teamharibo.goms.global.util.MemberUtil
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