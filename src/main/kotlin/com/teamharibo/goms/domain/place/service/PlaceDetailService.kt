package com.teamharibo.goms.domain.place.service

import com.teamharibo.goms.domain.place.dto.response.PlaceDetailResponse

interface PlaceDetailService {
    fun getDetail(placeId: Long): PlaceDetailResponse
}
