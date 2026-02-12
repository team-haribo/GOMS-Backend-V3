package com.example.team.haribo.goms.domain.outing.service.impl

import com.example.team.haribo.goms.domain.common.enums.Status
import com.example.team.haribo.goms.domain.outing.dto.response.MyOutingStatusResponse
import com.example.team.haribo.goms.domain.outing.repository.OutingRepository
import com.example.team.haribo.goms.domain.outing.service.MyOutingStatusService
import com.example.team.haribo.goms.global.util.MemberUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MyOutingStatusServiceImpl(
    private val memberUtil: MemberUtil,
    private val outingRepository: OutingRepository
) : MyOutingStatusService {

    @Transactional(readOnly = true)
    override fun getStatus(): MyOutingStatusResponse {
        val member = memberUtil.currentMember()
        val memberId = member.id!!

        val status = when (member.status) {
            Status.CANNOT_OUTING -> Status.CANNOT_OUTING
            else -> {
                val active = outingRepository.findTopByMemberIdAndComingAtIsNullOrderByIdDesc(memberId)
                if (active != null) Status.OUTING else Status.COMING
            }
        }

        return MyOutingStatusResponse(
            memberId = memberId,
            status = status,
            name = member.name,
            grade = member.grade,
            department = member.department.name
        )
    }
}
