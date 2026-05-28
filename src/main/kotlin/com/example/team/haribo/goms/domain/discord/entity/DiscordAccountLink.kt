package com.example.team.haribo.goms.domain.discord.entity

import com.example.team.haribo.goms.domain.member.entity.Member
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(
    name = "discord_account_link",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_discord_account_link_member", columnNames = ["member_id"]),
        UniqueConstraint(name = "uk_discord_account_link_discord_user", columnNames = ["discord_user_id"])
    ]
)
class DiscordAccountLink(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    var member: Member,

    @Column(name = "discord_user_id", nullable = false, length = 30)
    var discordUserId: String,

    @Column(name = "discord_username", nullable = false)
    var discordUsername: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    var createdAt: LocalDateTime? = null

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
}