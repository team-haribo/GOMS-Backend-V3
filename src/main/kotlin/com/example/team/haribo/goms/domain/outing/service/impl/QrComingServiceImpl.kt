package com.example.team.haribo.goms.domain.outing.service.impl

import com.example.team.haribo.goms.domain.common.enums.Action
import com.example.team.haribo.goms.domain.common.enums.Status
import com.example.team.haribo.goms.domain.outing.dto.request.QrToggleRequest
import com.example.team.haribo.goms.domain.outing.dto.response.QrComingResponse
import com.example.team.haribo.goms.domain.outing.exception.NotOutingException
import com.example.team.haribo.goms.domain.outing.repository.OutingRepository
import com.example.team.haribo.goms.domain.outing.service.QrComingService
import com.example.team.haribo.goms.domain.outing.util.QrExpValidator
import com.example.team.haribo.goms.global.log.LogFormat
import com.example.team.haribo.goms.global.util.MemberUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class QrComingServiceImpl(
    private val memberUtil: MemberUtil,
    private val outingRepository: OutingRepository
) : QrComingService {

    private val log = LoggerFactory.getLogger(QrComingServiceImpl::class.java)

    @Transactional
    override fun coming(request: QrToggleRequest): QrComingResponse {
        QrExpValidator.validate(request.exp)

        val member = memberUtil.currentMember()
        val memberId = member.id!!
        val beforeStatus = member.status

        log.info(
            LogFormat.message(
                domain = "OUTING",
                event = "복귀 시도",
                "memberId" to member.id,
                "name" to member.name
            )
        )

        val active = outingRepository.findTopByMemberIdAndComingAtIsNullOrderByIdDesc(memberId)
            ?: run {
                log.warn(
                    LogFormat.message(
                        domain = "OUTING",
                        event = "복귀 실패",
                        "memberId" to member.id,
                        "name" to member.name,
                        "reason" to "활성 외출 없음"
                    )
                )
                throw NotOutingException()
            }

        val now = LocalDateTime.now()
        active.comingAt = now
        member.status = Status.COMING

        log.info(
            LogFormat.message(
                domain = "OUTING",
                event = "복귀 처리",
                "memberId" to member.id,
                "name" to member.name,
                "status" to LogFormat.transition(beforeStatus, member.status),
                "outingId" to active.id
            )
        )

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