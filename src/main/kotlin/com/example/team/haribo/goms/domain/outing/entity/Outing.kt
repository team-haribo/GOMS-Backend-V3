package com.example.team.haribo.goms.domain.outing.entity

import com.example.team.haribo.goms.domain.member.entity.Member
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "outing",
    indexes = [
        // findAllActiveWithMember / countActive / searchActiveWithMemberByName 등
        // "WHERE coming_at IS NULL" 로 활성 외출만 조회하는 쿼리가 outing 테이블 전체를
        // Full Scan 하지 않도록 하기 위한 인덱스. outing row는 정리 배치 없이 계속 누적되므로
        // 데이터가 쌓일수록 이 인덱스의 효과가 커진다.
        Index(name = "idx_outing_coming_at", columnList = "coming_at")
    ]
)
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
