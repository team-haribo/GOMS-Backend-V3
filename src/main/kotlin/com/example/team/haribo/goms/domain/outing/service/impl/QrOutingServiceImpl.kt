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
import com.example.team.haribo.goms.global.log.LogFormat
import com.example.team.haribo.goms.global.util.MemberUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class QrOutingServiceImpl(
    private val memberUtil: MemberUtil,
    private val outingRepository: OutingRepository
) : QrOutingService {

    private val log = LoggerFactory.getLogger(QrOutingServiceImpl::class.java)

    @Transactional
    override fun outing(request: QrToggleRequest): QrOutingResponse {
        QrExpValidator.validate(request.exp)

        val member = memberUtil.currentMember()
        val beforeStatus = member.status

        log.info(
            LogFormat.message(
                domain = "OUTING",
                event = "외출 시도",
                "memberId" to member.id,
                "name" to member.name
            )
        )

        if (member.status == Status.CANNOT_OUTING) {
            log.warn(
                LogFormat.message(
                    domain = "OUTING",
                    event = "외출 실패",
                    "memberId" to member.id,
                    "name" to member.name,
                    "reason" to "외출 불가 상태",
                    "currentStatus" to member.status
                )
            )
            throw CannotOutingException()
        }

        if (member.status == Status.OUTING) {
            log.warn(
                LogFormat.message(
                    domain = "OUTING",
                    event = "외출 실패",
                    "memberId" to member.id,
                    "name" to member.name,
                    "reason" to "이미 외출 중"
                )
            )
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

        log.info(
            LogFormat.message(
                domain = "OUTING",
                event = "외출 처리",
                "memberId" to member.id,
                "name" to member.name,
                "status" to LogFormat.transition(beforeStatus, member.status),
                "outingId" to outing.id
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