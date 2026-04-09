package com.example.team.haribo.goms.domain.place.util

import com.example.team.haribo.goms.domain.place.dto.response.KakaoPlaceDocument
import org.springframework.stereotype.Component

@Component
class PlaceSyncFilter {

    private val excludedKeywords = listOf(
        "어린이집",
        "유치원",
        "학교",
        "학원",
        "주차장",
        "주유소",
        "충전소",
        "지하철역",
        "은행",
        "공공기관",
        "숙박"
    )

    fun isAllowed(document: KakaoPlaceDocument): Boolean {
        val categoryName = document.category_name.orEmpty()
        val placeName = document.place_name

        return excludedKeywords.none {
            categoryName.contains(it) || placeName.contains(it)
        }
    }
}