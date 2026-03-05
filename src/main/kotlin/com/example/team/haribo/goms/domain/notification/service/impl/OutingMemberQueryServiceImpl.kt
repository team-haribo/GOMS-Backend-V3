package com.example.team.haribo.goms.domain.notification.service.impl

import com.example.team.haribo.goms.domain.common.enums.Status
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.domain.notification.service.OutingMemberQueryService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OutingMemberQueryServiceImpl(
    private val memberRepository: MemberRepository
) : OutingMemberQueryService {

    @Transactional(readOnly = true)
    override fun getOutingMemberIds(): List<Long> {
        return memberRepository.findAllByStatus(Status.OUTING)
            .mapNotNull { it.id }
    }
}