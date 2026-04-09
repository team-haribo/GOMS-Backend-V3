package com.example.team.haribo.goms.domain.place.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "place")
class Place(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "external_place_id", nullable = false, unique = true, length = 100)
    val externalPlaceId: String,

    @Column(name = "place_name", nullable = false)
    var placeName: String,

    @Column(nullable = false)
    var address: String,

    @Column(name = "road_address")
    var roadAddress: String? = null,

    @Column(nullable = false)
    var latitude: Double,

    @Column(nullable = false)
    var longitude: Double,

    @Column(name = "category_group_code")
    var categoryGroupCode: String? = null,

    @Column(name = "category_group_name")
    var categoryGroupName: String? = null,

    @Column(name = "category_name")
    var categoryName: String? = null,

    @Column(name = "phone")
    var phone: String? = null,

    @Column(name = "place_url")
    var placeUrl: String? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "last_synced_at", nullable = false)
    var lastSyncedAt: LocalDateTime,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun sync(
        placeName: String,
        address: String,
        roadAddress: String?,
        latitude: Double,
        longitude: Double,
        categoryGroupCode: String?,
        categoryGroupName: String?,
        categoryName: String?,
        phone: String?,
        placeUrl: String?,
        syncedAt: LocalDateTime
    ) {
        this.placeName = placeName
        this.address = address
        this.roadAddress = roadAddress
        this.latitude = latitude
        this.longitude = longitude
        this.categoryGroupCode = categoryGroupCode
        this.categoryGroupName = categoryGroupName
        this.categoryName = categoryName
        this.phone = phone
        this.placeUrl = placeUrl
        this.isActive = true
        this.lastSyncedAt = syncedAt
        this.updatedAt = syncedAt
    }

    fun deactivate(now: LocalDateTime) {
        this.isActive = false
        this.updatedAt = now
    }
}