package com.example.team.haribo.goms.domain.place.util

import org.springframework.stereotype.Component
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@Component
class PlaceDistanceCalculator {

    fun isWithinRadius(
        originLatitude: Double,
        originLongitude: Double,
        targetLatitude: Double,
        targetLongitude: Double,
        radiusMeter: Int
    ): Boolean {
        return distanceMeter(
            originLatitude = originLatitude,
            originLongitude = originLongitude,
            targetLatitude = targetLatitude,
            targetLongitude = targetLongitude
        ) <= radiusMeter
    }

    fun distanceMeter(
        originLatitude: Double,
        originLongitude: Double,
        targetLatitude: Double,
        targetLongitude: Double
    ): Double {
        val earthRadiusMeter = 6_371_000.0

        val latitudeDelta = Math.toRadians(targetLatitude - originLatitude)
        val longitudeDelta = Math.toRadians(targetLongitude - originLongitude)
        val originLatitudeRad = Math.toRadians(originLatitude)
        val targetLatitudeRad = Math.toRadians(targetLatitude)

        val a = sin(latitudeDelta / 2).pow(2) +
                cos(originLatitudeRad) * cos(targetLatitudeRad) * sin(longitudeDelta / 2).pow(2)

        val c = 2 * asin(sqrt(a))

        return earthRadiusMeter * c
    }
}