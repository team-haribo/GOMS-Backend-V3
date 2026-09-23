package com.teamharibo.goms.domain.place.service

import com.teamharibo.goms.domain.place.dto.response.PlacesResponse

interface PlaceListService {
    fun getPlaces(): PlacesResponse
}