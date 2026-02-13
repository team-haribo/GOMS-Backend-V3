package com.example.team.haribo.goms.domain.outing.service.impl

import com.example.team.haribo.goms.domain.outing.dto.response.MyOutingStatusResponse
import com.example.team.haribo.goms.domain.outing.service.MyOutingStatusService
import com.example.team.haribo.goms.global.util.MemberUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MyOutingStatusServiceImpl(
    private val memberUtil: MemberUtil
) : MyOutingStatusService {

    @Transactional(readOnly = true)
    override fun getStatus(): MyOutingStatusResponse {
        val member = memberUtil.currentMember()

        return MyOutingStatusResponse(
            memberId = member.id!!,
            status = member.status,
            name = member.name,
            grade = member.grade,
            department = member.department.name
        )
    }
}
