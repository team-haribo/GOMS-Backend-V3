package com.example.team.haribo.goms.domain.auth.entity

import com.example.team.haribo.goms.domain.common.enums.Purpose
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "email_verification",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["email", "purpose"])
    ]
)
class EmailVerification(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_verification_id")
    val id: Long = 0,

    @Column(nullable = false)
    val email: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val purpose: Purpose,

    @Column(nullable = false)
    var code: String,

    @Column(name = "code_expires_at", nullable = false)
    var codeExpiresAt: LocalDateTime,

    @Column(name = "verified_token")
    var verifiedToken: String? = null,

    @Column(name = "verified_token_expires_at")
    var verifiedTokenExpiresAt: LocalDateTime? = null,

    @Column(name = "last_sent_at")
    var lastSentAt: LocalDateTime? = null
)
