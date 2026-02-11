package com.example.team.haribo.goms.domain.auth.repository

import com.example.team.haribo.goms.domain.auth.entity.EmailVerification
import com.example.team.haribo.goms.domain.common.enums.Purpose
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface EmailVerificationRepository : JpaRepository<EmailVerification, Long> {

    fun findByEmailAndPurpose(email: String, purpose: Purpose): Optional<EmailVerification>
}
