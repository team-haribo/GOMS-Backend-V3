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
import com.example.team.haribo.goms.global.log.LogFormat
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class StudentCouncilForceOutServiceImpl(
    private val memberRepository: MemberRepository,
    private val outingRepository: OutingRepository
) : StudentCouncilForceOutService {

    private val log = LoggerFactory.getLogger(StudentCouncilForceOutServiceImpl::class.java)

    @Transactional
    override fun out(memberId: Long): QrOutingResponse {
        log.info(
            LogFormat.message(
                domain = "STUDENT_COUNCIL",
                event = "강제 외출 시도",
                "memberId" to memberId
            )
        )

        val member = memberRepository.findById(memberId).orElseThrow {
            log.warn(
                LogFormat.message(
                    domain = "STUDENT_COUNCIL",
                    event = "강제 외출 실패",
                    "memberId" to memberId,
                    "reason" to "존재하지 않는 사용자"
                )
            )
            NotFoundMemberException()
        }

        if (member.status == Status.CANNOT_OUTING) {
            log.warn(
                LogFormat.message(
                    domain = "STUDENT_COUNCIL",
                    event = "강제 외출 실패",
                    "memberId" to memberId,
                    "reason" to "외출 불가 상태"
                )
            )
            throw CannotOutingException()
        }

        if (member.status == Status.OUTING) {
            log.warn(
                LogFormat.message(
                    domain = "STUDENT_COUNCIL",
                    event = "강제 외출 실패",
                    "memberId" to memberId,
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
                domain = "STUDENT_COUNCIL",
                event = "강제 외출 완료",
                "memberId" to memberId,
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