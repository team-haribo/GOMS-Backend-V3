package com.example.team.haribo.goms.domain.studentcouncil.service.impl

import com.example.team.haribo.goms.domain.common.enums.Status
import com.example.team.haribo.goms.domain.member.exception.NotFoundMemberException
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.domain.studentcouncil.exception.StatusConflictException
import com.example.team.haribo.goms.domain.studentcouncil.service.StudentCouncilOutingAllowedService
import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StudentCouncilOutingAllowedServiceImpl(
    private val memberRepository: MemberRepository
) : StudentCouncilOutingAllowedService {

    @Transactional
    override fun update(memberId: Long, status: Status) {
        if (status == Status.OUTING) throw GlobalException(ErrorCode.INVALID_REQUEST)

        val member = memberRepository.findById(memberId).orElseThrow { NotFoundMemberException() }

        if (member.status == Status.OUTING) throw StatusConflictException()
        if (member.status == status) throw StatusConflictException()

        member.status = status
    }
}
