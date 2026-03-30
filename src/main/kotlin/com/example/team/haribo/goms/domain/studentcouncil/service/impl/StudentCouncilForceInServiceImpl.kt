package com.example.team.haribo.goms.domain.studentcouncil.service.impl

import com.example.team.haribo.goms.domain.common.enums.Action
import com.example.team.haribo.goms.domain.common.enums.Status
import com.example.team.haribo.goms.domain.member.exception.NotFoundMemberException
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.domain.outing.dto.response.QrComingResponse
import com.example.team.haribo.goms.domain.outing.exception.NotOutingException
import com.example.team.haribo.goms.domain.outing.repository.OutingRepository
import com.example.team.haribo.goms.domain.studentcouncil.service.StudentCouncilForceInService
import com.example.team.haribo.goms.global.log.LogFormat
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class StudentCouncilForceInServiceImpl(
    private val memberRepository: MemberRepository,
    private val outingRepository: OutingRepository
) : StudentCouncilForceInService {

    private val log = LoggerFactory.getLogger(StudentCouncilForceInServiceImpl::class.java)

    @Transactional
    override fun `in`(memberId: Long): QrComingResponse {
        log.info(
            LogFormat.message(
                domain = "STUDENT_COUNCIL",
                event = "강제 복귀 시도",
                "memberId" to memberId
            )
        )

        val member = memberRepository.findById(memberId).orElseThrow {
            log.warn(
                LogFormat.message(
                    domain = "STUDENT_COUNCIL",
                    event = "강제 복귀 실패",
                    "memberId" to memberId,
                    "reason" to "존재하지 않는 사용자"
                )
            )
            NotFoundMemberException()
        }

        val active = outingRepository.findTopByMemberIdAndComingAtIsNullOrderByIdDesc(memberId)
            ?: run {
                log.warn(
                    LogFormat.message(
                        domain = "STUDENT_COUNCIL",
                        event = "강제 복귀 실패",
                        "memberId" to memberId,
                        "reason" to "외출 중 아님"
                    )
                )
                throw NotOutingException()
            }

        val now = LocalDateTime.now()
        active.comingAt = now
        member.status = Status.COMING

        log.info(
            LogFormat.message(
                domain = "STUDENT_COUNCIL",
                event = "강제 복귀 완료",
                "memberId" to memberId,
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