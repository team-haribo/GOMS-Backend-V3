package com.example.team.haribo.goms.domain.studentcouncil.service.impl

import com.example.team.haribo.goms.domain.common.enums.Role
import com.example.team.haribo.goms.domain.member.exception.NotFoundMemberException
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.domain.studentcouncil.exception.RoleConflictException
import com.example.team.haribo.goms.domain.studentcouncil.service.StudentCouncilRoleUpdateService
import com.example.team.haribo.goms.global.log.LogFormat
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StudentCouncilRoleUpdateServiceImpl(
    private val memberRepository: MemberRepository
) : StudentCouncilRoleUpdateService {

    private val log = LoggerFactory.getLogger(StudentCouncilRoleUpdateServiceImpl::class.java)

    @Transactional
    override fun update(memberId: Long, role: Role) {
        log.info(
            LogFormat.message(
                domain = "STUDENT_COUNCIL",
                event = "권한 변경 시도",
                "memberId" to memberId,
                "requestRole" to role
            )
        )

        val member = memberRepository.findById(memberId).orElseThrow {
            log.warn(
                LogFormat.message(
                    domain = "STUDENT_COUNCIL",
                    event = "권한 변경 실패",
                    "memberId" to memberId,
                    "reason" to "존재하지 않는 사용자"
                )
            )
            NotFoundMemberException()
        }

        if (member.role == role) {
            log.warn(
                LogFormat.message(
                    domain = "STUDENT_COUNCIL",
                    event = "권한 변경 실패",
                    "memberId" to memberId,
                    "reason" to "동일한 권한"
                )
            )
            throw RoleConflictException()
        }

        member.role = role

        log.info(
            LogFormat.message(
                domain = "STUDENT_COUNCIL",
                event = "권한 변경 완료",
                "memberId" to memberId,
                "role" to member.role
            )
        )
    }
}