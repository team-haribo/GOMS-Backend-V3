package com.example.team.haribo.goms.domain.studentcouncil.service.impl

import com.example.team.haribo.goms.domain.common.enums.Status
import com.example.team.haribo.goms.domain.member.exception.NotFoundMemberException
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.domain.studentcouncil.exception.StatusConflictException
import com.example.team.haribo.goms.domain.studentcouncil.service.StudentCouncilOutingAllowedService
import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException
import com.example.team.haribo.goms.global.log.LogFormat
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StudentCouncilOutingAllowedServiceImpl(
    private val memberRepository: MemberRepository
) : StudentCouncilOutingAllowedService {

    private val log = LoggerFactory.getLogger(StudentCouncilOutingAllowedServiceImpl::class.java)

    @Transactional
    override fun update(memberId: Long, status: Status) {
        log.info(
            LogFormat.message(
                domain = "STUDENT_COUNCIL",
                event = "외출 상태 변경 시도",
                "memberId" to memberId,
                "requestStatus" to status
            )
        )

        if (status == Status.OUTING) {
            log.warn(
                LogFormat.message(
                    domain = "STUDENT_COUNCIL",
                    event = "외출 상태 변경 실패",
                    "memberId" to memberId,
                    "reason" to "OUTING 상태로 직접 변경 불가"
                )
            )
            throw GlobalException(ErrorCode.INVALID_REQUEST)
        }

        val member = memberRepository.findById(memberId).orElseThrow {
            log.warn(
                LogFormat.message(
                    domain = "STUDENT_COUNCIL",
                    event = "외출 상태 변경 실패",
                    "memberId" to memberId,
                    "reason" to "존재하지 않는 사용자"
                )
            )
            NotFoundMemberException()
        }

        if (member.status == Status.OUTING) {
            log.warn(
                LogFormat.message(
                    domain = "STUDENT_COUNCIL",
                    event = "외출 상태 변경 실패",
                    "memberId" to memberId,
                    "reason" to "현재 외출 중"
                )
            )
            throw StatusConflictException()
        }

        if (member.status == status) {
            log.warn(
                LogFormat.message(
                    domain = "STUDENT_COUNCIL",
                    event = "외출 상태 변경 실패",
                    "memberId" to memberId,
                    "reason" to "동일한 상태"
                )
            )
            throw StatusConflictException()
        }

        member.status = status

        log.info(
            LogFormat.message(
                domain = "STUDENT_COUNCIL",
                event = "외출 상태 변경 완료",
                "memberId" to memberId,
                "status" to member.status
            )
        )
    }
}