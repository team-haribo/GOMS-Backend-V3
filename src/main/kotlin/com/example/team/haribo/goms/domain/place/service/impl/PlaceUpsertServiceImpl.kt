package com.example.team.haribo.goms.domain.place.service.impl

import com.example.team.haribo.goms.domain.place.dto.request.PlaceUpsertRequest
import com.example.team.haribo.goms.domain.place.dto.response.PlaceUpsertResponse
import com.example.team.haribo.goms.domain.place.entity.Place
import com.example.team.haribo.goms.domain.place.repository.PlaceRepository
import com.example.team.haribo.goms.domain.place.service.PlaceUpsertService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PlaceUpsertServiceImpl(
    private val placeRepository: PlaceRepository
) : PlaceUpsertService {

    @Transactional
    override fun upsert(request: PlaceUpsertRequest): PlaceUpsertResponse {
        val existing = placeRepository.findByLatitudeAndLongitude(request.latitude, request.longitude).orElse(null)

        if (existing != null) {
            return PlaceUpsertResponse(placeId = existing.id!!)
        }

        val saved = placeRepository.save(
            Place(
                latitude = request.latitude,
                longitude = request.longitude
            )
        )

        return PlaceUpsertResponse(placeId = saved.id!!)
    }
}
