package com.example.team.haribo.goms.domain.place.service

import com.example.team.haribo.goms.domain.place.dto.response.PlacesResponse

interface PlaceHotPlaceService {
    fun getHotPlaces(): PlacesResponse
}
