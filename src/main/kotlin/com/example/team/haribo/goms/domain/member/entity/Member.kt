package com.example.team.haribo.goms.domain.member.entity

import com.example.team.haribo.goms.domain.common.enums.Role
import jakarta.persistence.*

@Entity
@Table(name = "member")
class Member(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false)
    var password: String,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var grade: Int,

    @Column(nullable = false)
    var department: String,

    @Column(nullable = false)
    var gender: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role
)
