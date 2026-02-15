package com.example.team.haribo.goms.domain.studentcouncil.service.impl

import com.example.team.haribo.goms.domain.common.enums.Action
import com.example.team.haribo.goms.domain.common.enums.Status
import com.example.team.haribo.goms.domain.member.exception.NotFoundMemberException
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.domain.outing.dto.response.QrOutingResponse
import com.example.team.haribo.goms.domain.outing.entity.Outing
import com.example.team.haribo.goms.domain.outing.exception.AlreadyOutingException
import com.example.team.haribo.goms.domain.outing.exception.CannotOutingException
import com.example.team.haribo.goms.domain.outing.repository.OutingRepository
import com.example.team.haribo.goms.domain.studentcouncil.service.StudentCouncilForceOutService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class StudentCouncilForceOutServiceImpl(
    private val memberRepository: MemberRepository,
    private val outingRepository: OutingRepository
) : StudentCouncilForceOutService {

    @Transactional
    override fun out(memberId: Long): QrOutingResponse {
        val member = memberRepository.findById(memberId).orElseThrow { NotFoundMemberException() }

        if (member.status == Status.CANNOT_OUTING) throw CannotOutingException()
        if (member.status == Status.OUTING) throw AlreadyOutingException()

        val now = LocalDateTime.now()
        member.status = Status.OUTING

        val outing = outingRepository.save(
            Outing(
                member = member,
                outingAt = now
            )
        )

        return QrOutingResponse(
            action = Action.OUT,
            outingId = outing.id!!,
            status = member.status,
            outingAt = now
        )
    }
}
