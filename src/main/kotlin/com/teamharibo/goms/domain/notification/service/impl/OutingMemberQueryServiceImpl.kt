package com.teamharibo.goms.domain.notification.service.impl

import com.teamharibo.goms.domain.common.enums.Status
import com.teamharibo.goms.domain.member.repository.MemberRepository
import com.teamharibo.goms.domain.notification.service.OutingMemberQueryService
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