package com.example.team.haribo.goms.domain.place.service

import com.example.team.haribo.goms.domain.place.dto.response.PlaceDetailResponse

interface PlaceDetailService {
    fun getDetail(placeId: Long): PlaceDetailResponse
}
