package com.teamharibo.goms.domain.place.service

import com.teamharibo.goms.domain.place.dto.response.PlacesResponse
import com.teamharibo.goms.domain.place.dto.response.RecommendCountResponse
import com.teamharibo.goms.domain.place.dto.response.RecommendResponse

interface PlaceRecommendService {
    fun recommend(placeId: Long): RecommendResponse
    fun unrecommend(placeId: Long): RecommendResponse
    fun getRecommendedPlaces(): PlacesResponse
    fun getRecommendedCount(): RecommendCountResponse
}
