package com.example.team.haribo.goms.domain.studentcouncil.service.impl

import com.example.team.haribo.goms.domain.common.enums.Action
import com.example.team.haribo.goms.domain.common.enums.Status
import com.example.team.haribo.goms.domain.member.exception.NotFoundMemberException
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.domain.outing.dto.response.QrComingResponse
import com.example.team.haribo.goms.domain.outing.exception.NotOutingException
import com.example.team.haribo.goms.domain.outing.repository.OutingRepository
import com.example.team.haribo.goms.domain.studentcouncil.service.StudentCouncilForceInService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class StudentCouncilForceInServiceImpl(
    private val memberRepository: MemberRepository,
    private val outingRepository: OutingRepository
) : StudentCouncilForceInService {

    @Transactional
    override fun `in`(memberId: Long): QrComingResponse {
        val member = memberRepository.findById(memberId).orElseThrow { NotFoundMemberException() }

        val active = outingRepository.findTopByMemberIdAndComingAtIsNullOrderByIdDesc(memberId)
            ?: throw NotOutingException()

        val now = LocalDateTime.now()
        active.comingAt = now
        member.status = Status.COMING

        return QrComingResponse(
            action = Action.IN,
            outingId = active.id!!,
            status = member.status,
            comingAt = now,
            lateCreated = false,
            lateId = null
        )
    }
}
