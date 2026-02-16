package com.example.team.haribo.goms.domain.studentcouncil.service.impl

import com.example.team.haribo.goms.domain.common.enums.Role
import com.example.team.haribo.goms.domain.member.exception.NotFoundMemberException
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.domain.studentcouncil.exception.RoleConflictException
import com.example.team.haribo.goms.domain.studentcouncil.service.StudentCouncilRoleUpdateService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StudentCouncilRoleUpdateServiceImpl(
    private val memberRepository: MemberRepository
) : StudentCouncilRoleUpdateService {

    @Transactional
    override fun update(memberId: Long, role: Role) {
        val member = memberRepository.findById(memberId).orElseThrow { NotFoundMemberException() }
        if (member.role == role) throw RoleConflictException()
        member.role = role
    }
}
