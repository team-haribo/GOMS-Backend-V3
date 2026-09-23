package com.teamharibo.goms.domain.place.service

import com.teamharibo.goms.domain.place.dto.response.PlaceSearchListResponse

interface PlaceSearchService {
    fun search(keyword: String?): PlaceSearchListResponse
}
