package com.example.team.haribo.goms.global.util

import com.example.team.haribo.goms.domain.member.entity.Member
import com.example.team.haribo.goms.domain.member.exception.NotFoundMemberException
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class MemberUtil(
    private val memberRepository: MemberRepository
) {

    fun currentMemberId(): Long {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw GlobalException(ErrorCode.UNAUTHORIZED)

        val principal = authentication.principal
            ?: throw GlobalException(ErrorCode.UNAUTHORIZED)

        return when (principal) {
            is Long -> principal
            is Int -> principal.toLong()
            is String -> principal.toLongOrNull() ?: findMemberIdByEmail(principal)
            else -> throw GlobalException(ErrorCode.UNAUTHORIZED)
        }
    }

    fun currentMember(): Member {
        val memberId = currentMemberId()
        return memberRepository.findById(memberId).orElseThrow { NotFoundMemberException() }
    }

    private fun findMemberIdByEmail(email: String): Long {
        val member = memberRepository.findByEmail(email).orElseThrow { NotFoundMemberException() }
        return member.id!!
    }
}
