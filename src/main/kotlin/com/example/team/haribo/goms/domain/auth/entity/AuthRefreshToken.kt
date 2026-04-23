package com.example.team.haribo.goms.domain.auth.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "auth_refresh_token")
class AuthRefreshToken(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auth_refresh_token_id")
    val id: Long = 0,

    @Column(name = "member_id", nullable = false, unique = true)
    val memberId: Long,

    @Column(nullable = false, length = 500)
    var refreshToken: String,

    @Column(name = "expires_at", nullable = false)
    var expiresAt: LocalDateTime,

    @Column(name = "revoked_at")
    var revokedAt: LocalDateTime? = null
)
