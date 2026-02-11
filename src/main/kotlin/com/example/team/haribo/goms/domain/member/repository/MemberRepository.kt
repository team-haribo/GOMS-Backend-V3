package com.example.team.haribo.goms.domain.member.repository

import com.example.team.haribo.goms.domain.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface MemberRepository : JpaRepository<Member, Long> {

    fun findByEmail(email: String): Optional<Member>

    fun existsByEmail(email: String): Boolean
}
