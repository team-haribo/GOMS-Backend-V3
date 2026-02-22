package com.example.team.haribo.goms.domain.place.repository

import com.example.team.haribo.goms.domain.place.entity.Place
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface PlaceRepository : JpaRepository<Place, Long> {

    fun findByLatitudeAndLongitude(latitude: Double, longitude: Double): Optional<Place>

    @Query(
        """
        SELECT p
        FROM Place p
        WHERE p.placeName LIKE CONCAT('%', :keyword, '%')
           OR p.address LIKE CONCAT('%', :keyword, '%')
        ORDER BY p.id DESC
        """
    )
    fun searchByKeyword(keyword: String): List<Place>
}
