package com.example.team.haribo.goms.domain.member.entity

import com.example.team.haribo.goms.domain.common.enums.Department
import com.example.team.haribo.goms.domain.common.enums.Gender
import com.example.team.haribo.goms.domain.common.enums.Role
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "member")
class Member(
    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false)
    var password: String,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var grade: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var department: Department,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var gender: Gender,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    var createdAt: LocalDateTime? = null
}
