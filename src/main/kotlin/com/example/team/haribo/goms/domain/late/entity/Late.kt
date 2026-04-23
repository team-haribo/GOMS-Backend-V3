package com.example.team.haribo.goms.domain.late.entity

import com.example.team.haribo.goms.domain.member.entity.Member
import com.example.team.haribo.goms.domain.outing.entity.Outing
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "late")
class Late(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    var member: Member,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outing_id", nullable = false)
    var outing: Outing,

    @Column(name = "coming_at", nullable = false)
    var comingAt: LocalDateTime,

    @Column(name = "late_count", nullable = false)
    var lateCount: Long
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}
