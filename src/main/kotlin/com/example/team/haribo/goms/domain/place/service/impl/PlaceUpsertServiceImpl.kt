package com.example.team.haribo.goms.domain.place.service.impl

import com.example.team.haribo.goms.domain.place.dto.request.PlaceUpsertRequest
import com.example.team.haribo.goms.domain.place.dto.response.PlaceUpsertResponse
import com.example.team.haribo.goms.domain.place.entity.Place
import com.example.team.haribo.goms.domain.place.repository.PlaceRepository
import com.example.team.haribo.goms.domain.place.service.PlaceUpsertService
import com.example.team.haribo.goms.global.log.LogFormat
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PlaceUpsertServiceImpl(
    private val placeRepository: PlaceRepository
) : PlaceUpsertService {

    private val log = LoggerFactory.getLogger(PlaceUpsertServiceImpl::class.java)

    @Transactional
    override fun upsert(request: PlaceUpsertRequest): PlaceUpsertResponse {
        log.info(
            LogFormat.message(
                domain = "PLACE",
                event = "장소 저장 시도",
                "placeName" to request.placeName,
                "latitude" to request.latitude,
                "longitude" to request.longitude
            )
        )

        val existing = placeRepository.findByLatitudeAndLongitude(request.latitude, request.longitude).orElse(null)

        if (existing != null) {
            existing.placeName = request.placeName
            existing.address = request.address
            val saved = placeRepository.save(existing)

            log.info(
                LogFormat.message(
                    domain = "PLACE",
                    event = "장소 수정 완료",
                    "placeId" to saved.id,
                    "placeName" to saved.placeName
                )
            )

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

            log.info(
                LogFormat.message(
                    domain = "PLACE",
                    event = "장소 등록 완료",
                    "placeId" to saved.id,
                    "placeName" to saved.placeName
                )
            )

            PlaceUpsertResponse(placeId = saved.id!!)
        } catch (e: DataIntegrityViolationException) {
            val found = placeRepository.findByLatitudeAndLongitude(request.latitude, request.longitude).orElseThrow()

            log.warn(
                LogFormat.message(
                    domain = "PLACE",
                    event = "장소 중복 저장 감지",
                    "placeId" to found.id,
                    "placeName" to found.placeName
                )
            )

            PlaceUpsertResponse(placeId = found.id!!)
        }
    }
}