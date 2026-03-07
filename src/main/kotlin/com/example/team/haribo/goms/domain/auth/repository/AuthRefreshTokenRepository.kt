package com.example.team.haribo.goms.domain.auth.repository

import com.example.team.haribo.goms.domain.auth.entity.AuthRefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface AuthRefreshTokenRepository : JpaRepository<AuthRefreshToken, Long> {

    fun findByMemberId(memberId: Long): Optional<AuthRefreshToken>

    fun findByRefreshToken(refreshToken: String): Optional<AuthRefreshToken>

    fun deleteAllByMemberId(memberId: Long): Long
}