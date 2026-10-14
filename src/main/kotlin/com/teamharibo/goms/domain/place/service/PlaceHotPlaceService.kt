package com.teamharibo.goms.domain.place.service

import com.teamharibo.goms.domain.place.dto.response.PlacesResponse

interface PlaceHotPlaceService {
    fun getHotPlaces(days: Long?): PlacesResponse
}
