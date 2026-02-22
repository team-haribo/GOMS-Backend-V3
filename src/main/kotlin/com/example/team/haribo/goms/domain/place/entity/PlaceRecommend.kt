package com.example.team.haribo.goms.domain.place.entity

import com.example.team.haribo.goms.domain.member.entity.Member
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "place_recommend",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["place_id", "member_id"])
    ]
)
class PlaceRecommend(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    val place: Place,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @Column(nullable = false)
    var recommended: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
