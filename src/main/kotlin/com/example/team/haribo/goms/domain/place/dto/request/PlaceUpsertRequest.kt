package com.example.team.haribo.goms.domain.place.dto.request

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank

data class PlaceUpsertRequest(
    @field:NotBlank(message = "placeName 은 비어 있을 수 없습니다.")
    val placeName: String,

    @field:NotBlank(message = "address 는 비어 있을 수 없습니다.")
    val address: String,

    @field:DecimalMin(value = "-90.0", message = "latitude 는 -90 이상이어야 합니다.")
    @field:DecimalMax(value = "90.0", message = "latitude 는 90 이하여야 합니다.")
    val latitude: Double,

    @field:DecimalMin(value = "-180.0", message = "longitude 는 -180 이상이어야 합니다.")
    @field:DecimalMax(value = "180.0", message = "longitude 는 180 이하여야 합니다.")
    val longitude: Double
)