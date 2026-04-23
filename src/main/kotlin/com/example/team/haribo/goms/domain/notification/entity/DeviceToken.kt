package com.example.team.haribo.goms.domain.notification.entity

import com.example.team.haribo.goms.domain.common.enums.Platform
import com.example.team.haribo.goms.domain.member.entity.Member
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(
    name = "device_token",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_member_device", columnNames = ["member_id", "device_id"]),
        UniqueConstraint(name = "uk_fcm_token", columnNames = ["fcm_token"])
    ],
    indexes = [
        Index(name = "idx_member_id", columnList = "member_id")
    ]
)
class DeviceToken(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @Column(name = "device_id", nullable = false, length = 128)
    var deviceId: String,

    @Column(name = "fcm_token", nullable = false, length = 512)
    var fcmToken: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false)
    var platform: Platform
) {

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null

    fun update(fcmToken: String, platform: Platform) {
        this.fcmToken = fcmToken
        this.platform = platform
    }
}