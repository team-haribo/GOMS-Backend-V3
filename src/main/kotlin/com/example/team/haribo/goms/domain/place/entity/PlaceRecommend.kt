package com.example.team.haribo.goms.domain.place.entity

import com.example.team.haribo.goms.domain.member.entity.Member
import jakarta.persistence.*
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(
    name = "place_recommend",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["place_id", "member_id"])
    ]
)
@EntityListeners(AuditingEntityListener::class)
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

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime,

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
)
