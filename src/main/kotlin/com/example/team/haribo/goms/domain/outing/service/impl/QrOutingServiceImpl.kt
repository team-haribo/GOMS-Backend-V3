package com.example.team.haribo.goms.domain.outing.service.impl

import com.example.team.haribo.goms.domain.common.enums.Action
import com.example.team.haribo.goms.domain.common.enums.Status
import com.example.team.haribo.goms.domain.outing.dto.request.QrToggleRequest
import com.example.team.haribo.goms.domain.outing.dto.response.QrOutingResponse
import com.example.team.haribo.goms.domain.outing.entity.Outing
import com.example.team.haribo.goms.domain.outing.exception.AlreadyOutingException
import com.example.team.haribo.goms.domain.outing.exception.CannotOutingException
import com.example.team.haribo.goms.domain.outing.repository.OutingRepository
import com.example.team.haribo.goms.domain.outing.service.QrOutingService
import com.example.team.haribo.goms.domain.outing.util.QrExpValidator
import com.example.team.haribo.goms.global.util.MemberUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class QrOutingServiceImpl(
    private val memberUtil: MemberUtil,
    private val outingRepository: OutingRepository
) : QrOutingService {

    @Transactional
    override fun outing(request: QrToggleRequest): QrOutingResponse {
        QrExpValidator.validate(request.exp)

        val member = memberUtil.currentMember()

        if (member.status == Status.CANNOT_OUTING) {
            throw CannotOutingException()
        }

        if (member.status == Status.OUTING) {
            throw AlreadyOutingException()
        }

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
