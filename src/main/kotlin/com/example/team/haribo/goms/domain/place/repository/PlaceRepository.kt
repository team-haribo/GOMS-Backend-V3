package com.example.team.haribo.goms.domain.place.repository

import com.example.team.haribo.goms.domain.place.entity.Place
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface PlaceRepository : JpaRepository<Place, Long> {
    fun findByLatitudeAndLongitude(latitude: Double, longitude: Double): Optional<Place>
}
