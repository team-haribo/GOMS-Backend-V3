package com.example.team.haribo.goms.domain.place.dto.response

data class KakaoPlaceDocument(
    val id: String,
    val place_name: String,
    val category_name: String?,
    val category_group_code: String?,
    val category_group_name: String?,
    val phone: String?,
    val address_name: String,
    val road_address_name: String?,
    val x: String,
    val y: String,
    val place_url: String?
)