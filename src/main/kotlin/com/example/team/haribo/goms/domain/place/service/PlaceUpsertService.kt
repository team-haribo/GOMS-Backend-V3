package com.example.team.haribo.goms.domain.place.service

import com.example.team.haribo.goms.domain.place.dto.request.PlaceUpsertRequest
import com.example.team.haribo.goms.domain.place.dto.response.PlaceUpsertResponse

interface PlaceUpsertService {
    fun upsert(request: PlaceUpsertRequest): PlaceUpsertResponse
}
