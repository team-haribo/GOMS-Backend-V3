package com.teamharibo.goms.domain.place.util

import com.teamharibo.goms.domain.place.dto.response.PlaceSearchResponse
import com.teamharibo.goms.domain.place.dto.response.PlaceSummaryResponse
import com.teamharibo.goms.domain.place.entity.Place
import org.springframework.stereotype.Component

@Component
class PlaceSummaryMapper {
    fun toSummary(
        place: Place,
        reviewCount: Long,
        recommendCount: Long,
        recommended: Boolean
    ): PlaceSummaryResponse {
        return PlaceSummaryResponse(
            placeId = place.id!!,
            placeName = place.placeName,
            address = place.address,
            roadAddress = place.roadAddress,
            latitude = place.latitude,
            longitude = place.longitude,
            categoryGroupName = place.categoryGroupName,
            categoryName = place.categoryName,
            reviewCount = reviewCount,
            recommendCount = recommendCount,
            recommended = recommended,
        )
    }

    fun toSearchResponse(
        place: Place,
        reviewCount: Long,
        recommendCount: Long,
        recommended: Boolean
    ): PlaceSearchResponse {
        return PlaceSearchResponse(
            placeId = place.id!!,
            placeName = place.placeName,
            address = place.address,
            roadAddress = place.roadAddress,
            latitude = place.latitude,
            longitude = place.longitude,
            categoryGroupName = place.categoryGroupName,
            categoryName = place.categoryName,
            reviewCount = reviewCount,
            recommendCount = recommendCount,
            recommended = recommended,
        )
    }
}