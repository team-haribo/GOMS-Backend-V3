package com.example.team.haribo.goms.domain.place.entity

import jakarta.persistence.*

@Entity
@Table(name = "place")
class Place(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var latitude: Double,

    @Column(nullable = false)
    var longitude: Double
)
