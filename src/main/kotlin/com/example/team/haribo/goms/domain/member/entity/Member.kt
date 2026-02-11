package com.example.team.haribo.goms.domain.member.entity

import com.example.team.haribo.goms.domain.common.enums.Department
import com.example.team.haribo.goms.domain.common.enums.Gender
import com.example.team.haribo.goms.domain.common.enums.Role
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "member")
class Member(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    var password: String,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val grade: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val department: Department,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val gender: Gender,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: Role = Role.ROLE_STUDENT,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
