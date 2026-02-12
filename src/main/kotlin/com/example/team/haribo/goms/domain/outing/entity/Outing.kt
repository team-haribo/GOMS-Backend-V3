package com.example.team.haribo.goms.domain.outing.entity

import com.example.team.haribo.goms.domain.member.entity.Member
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "outing")
class Outing(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    var member: Member,

    @Column(name = "outing_at", nullable = false, updatable = false)
    var outingAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "coming_at")
    var comingAt: LocalDateTime? = null
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}
