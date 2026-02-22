package com.example.team.haribo.goms.domain.place.service.impl

import com.example.team.haribo.goms.domain.place.dto.request.PlaceUpsertRequest
import com.example.team.haribo.goms.domain.place.dto.response.PlaceUpsertResponse
import com.example.team.haribo.goms.domain.place.entity.Place
import com.example.team.haribo.goms.domain.place.repository.PlaceRepository
import com.example.team.haribo.goms.domain.place.service.PlaceUpsertService
import org.springframework.dao.DataIntegrityViolationException
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
            existing.placeName = request.placeName
            existing.address = request.address
            val saved = placeRepository.save(existing)
            return PlaceUpsertResponse(placeId = saved.id!!)
        }

        return try {
            val saved = placeRepository.save(
                Place(
                    latitude = request.latitude,
                    longitude = request.longitude,
                    placeName = request.placeName,
                    address = request.address
                )
            )
            PlaceUpsertResponse(placeId = saved.id!!)
        } catch (e: DataIntegrityViolationException) {
            val found = placeRepository.findByLatitudeAndLongitude(request.latitude, request.longitude).orElseThrow()
            PlaceUpsertResponse(placeId = found.id!!)
        }
    }
}
