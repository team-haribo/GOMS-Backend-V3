package com.example.team.haribo.goms.domain.place.repository

import com.example.team.haribo.goms.domain.place.entity.Place
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime
import java.util.Optional

interface PlaceRepository : JpaRepository<Place, Long> {

    fun findByExternalPlaceId(externalPlaceId: String): Optional<Place>

    fun existsByIdAndIsActiveTrue(id: Long): Boolean

    fun findByIdAndIsActiveTrue(id: Long): Optional<Place>

    fun findAllByIsActiveTrue(): List<Place>

    fun findAllByLastSyncedAtBeforeAndIsActiveTrue(time: LocalDateTime): List<Place>

    @Query(
        """
        SELECT p
        FROM Place p
        WHERE p.isActive = true
          AND (
              p.placeName LIKE CONCAT('%', :keyword, '%')
              OR p.address LIKE CONCAT('%', :keyword, '%')
              OR p.roadAddress LIKE CONCAT('%', :keyword, '%')
          )
        ORDER BY p.id DESC
        """
    )
    fun searchByKeyword(keyword: String): List<Place>
}