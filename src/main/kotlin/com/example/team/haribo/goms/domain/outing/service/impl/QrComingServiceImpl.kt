package com.example.team.haribo.goms.domain.outing.service.impl

import com.example.team.haribo.goms.domain.common.enums.Action
import com.example.team.haribo.goms.domain.common.enums.Status
import com.example.team.haribo.goms.domain.outing.dto.request.QrToggleRequest
import com.example.team.haribo.goms.domain.outing.dto.response.QrComingResponse
import com.example.team.haribo.goms.domain.outing.exception.NotOutingException
import com.example.team.haribo.goms.domain.outing.repository.OutingRepository
import com.example.team.haribo.goms.domain.outing.service.QrComingService
import com.example.team.haribo.goms.domain.outing.util.QrExpValidator
import com.example.team.haribo.goms.global.util.MemberUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class QrComingServiceImpl(
    private val memberUtil: MemberUtil,
    private val outingRepository: OutingRepository
) : QrComingService {

    @Transactional
    override fun coming(request: QrToggleRequest): QrComingResponse {
        QrExpValidator.validate(request.exp)

        val member = memberUtil.currentMember()
        val memberId = member.id!!

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
