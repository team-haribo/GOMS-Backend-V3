package com.example.team.haribo.goms.domain.place.entity

import jakarta.persistence.*

@Entity
@Table(
    name = "place",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["latitude", "longitude"])
    ]
)
class Place(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val latitude: Double,

    @Column(nullable = false)
    val longitude: Double,

    @Column(name = "place_name", nullable = false)
    var placeName: String,

    @Column(nullable = false)
    var address: String
)
