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
        val northLatitude = moveLatitude(centerLatitude, offsetMeter)
        val southLatitude = moveLatitude(centerLatitude, -offsetMeter)
        val eastLongitude = moveLongitude(centerLatitude, centerLongitude, offsetMeter)
        val westLongitude = moveLongitude(centerLatitude, centerLongitude, -offsetMeter)

        return listOf(
            SearchPoint(centerLongitude, centerLatitude, searchRadius),
            SearchPoint(centerLongitude, northLatitude, searchRadius),
            SearchPoint(centerLongitude, southLatitude, searchRadius),
            SearchPoint(eastLongitude, centerLatitude, searchRadius),
            SearchPoint(westLongitude, centerLatitude, searchRadius),
            SearchPoint(eastLongitude, northLatitude, searchRadius),
            SearchPoint(westLongitude, northLatitude, searchRadius),
            SearchPoint(eastLongitude, southLatitude, searchRadius),
            SearchPoint(westLongitude, southLatitude, searchRadius)
        )
    }

    private fun moveLatitude(latitude: Double, meter: Double): Double {
        return latitude + (meter / 111_320.0)
    }

    private fun moveLongitude(latitude: Double, longitude: Double, meter: Double): Double {
        return longitude + (meter / (111_320.0 * cos(Math.toRadians(latitude))))
    }
}