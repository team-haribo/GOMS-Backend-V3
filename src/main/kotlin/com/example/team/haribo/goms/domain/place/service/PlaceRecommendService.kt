package com.example.team.haribo.goms.domain.place.service

import com.example.team.haribo.goms.domain.place.dto.response.PlacesResponse
import com.example.team.haribo.goms.domain.place.dto.response.RecommendCountResponse
import com.example.team.haribo.goms.domain.place.dto.response.RecommendResponse

interface PlaceRecommendService {
    fun recommend(placeId: Long): RecommendResponse
    fun unrecommend(placeId: Long): RecommendResponse
    fun getRecommendedPlaces(): PlacesResponse
    fun getRecommendedCount(): RecommendCountResponse
}
