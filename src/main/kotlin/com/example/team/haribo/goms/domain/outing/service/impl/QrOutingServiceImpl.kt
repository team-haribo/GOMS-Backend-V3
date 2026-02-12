package com.example.team.haribo.goms.domain.outing.service.impl

import com.example.team.haribo.goms.domain.common.enums.Status
import com.example.team.haribo.goms.domain.member.exception.NotFoundMemberException
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.domain.outing.dto.request.QrToggleRequest
import com.example.team.haribo.goms.domain.outing.dto.response.QrOutingResponse
import com.example.team.haribo.goms.domain.outing.entity.Outing
import com.example.team.haribo.goms.domain.outing.exception.AlreadyOutingException
import com.example.team.haribo.goms.domain.outing.exception.CannotOutingException
import com.example.team.haribo.goms.domain.outing.repository.OutingRepository
import com.example.team.haribo.goms.domain.outing.service.QrOutingService
import com.example.team.haribo.goms.domain.outing.util.QrExpValidator
import com.example.team.haribo.goms.global.security.SecurityUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class QrOutingServiceImpl(
    private val memberRepository: MemberRepository,
    private val outingRepository: OutingRepository
) : QrOutingService {

    @Transactional
    override fun outing(request: QrToggleRequest): QrOutingResponse {
        QrExpValidator.validate(request.exp)

        val memberId = SecurityUtil.getCurrentMemberId()
        val member = memberRepository.findById(memberId).orElseThrow { NotFoundMemberException() }

        if (member.status == Status.CANNOT_OUTING) {
            throw CannotOutingException()
        }

        val active = outingRepository.findTopByMemberIdAndComingAtIsNullOrderByIdDesc(memberId)
        if (active != null) {
            throw AlreadyOutingException()
        }

        val saved = outingRepository.save(Outing(member = member))
        member.status = Status.OUTING

        return QrOutingResponse(
            action = "OUT",
            outingId = saved.id!!,
            status = member.status,
            outingAt = saved.outingAt
        )
    }
}
