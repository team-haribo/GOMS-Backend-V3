package com.example.team.haribo.goms.domain.place.util

import org.springframework.stereotype.Component
import kotlin.math.cos

@Component
class PlaceSearchPointGenerator {

    fun generate(
        centerLatitude: Double,
        centerLongitude: Double,
        offsetMeter: Double,
        searchRadius: Int
    ): List<SearchPoint> {
        val points = mutableListOf<SearchPoint>()

        for (latitudeStep in -2..2) {
            for (longitudeStep in -2..2) {
                val movedLatitude = moveLatitude(centerLatitude, latitudeStep * offsetMeter)
                val movedLongitude = moveLongitude(centerLatitude, centerLongitude, longitudeStep * offsetMeter)

                points += SearchPoint(
                    longitude = movedLongitude,
                    latitude = movedLatitude,
                    radius = searchRadius
                )
            }
        }

        return points
    }

    private fun moveLatitude(latitude: Double, meter: Double): Double {
        return latitude + (meter / 111_320.0)
    }

    private fun moveLongitude(latitude: Double, longitude: Double, meter: Double): Double {
        return longitude + (meter / (111_320.0 * cos(Math.toRadians(latitude))))
    }
}