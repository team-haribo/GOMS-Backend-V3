package com.example.team.haribo.goms.domain.place.service.impl

import com.example.team.haribo.goms.domain.place.dto.response.PlaceSearchListResponse
import com.example.team.haribo.goms.domain.place.dto.response.PlaceSearchResponse
import com.example.team.haribo.goms.domain.place.repository.PlaceRecommendRepository
import com.example.team.haribo.goms.domain.place.repository.PlaceRepository
import com.example.team.haribo.goms.domain.place.service.PlaceSearchService
import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException
import com.example.team.haribo.goms.global.util.MemberUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PlaceSearchServiceImpl(
    private val placeRepository: PlaceRepository,
    private val recommendRepository: PlaceRecommendRepository,
    private val memberUtil: MemberUtil
) : PlaceSearchService {

    @Transactional(readOnly = true)
    override fun search(keyword: String?): PlaceSearchListResponse {
        val key = keyword?.trim()?.takeIf { it.isNotBlank() } ?: throw GlobalException(ErrorCode.INVALID_REQUEST)

        val memberId = memberUtil.currentMemberId()
        val places = placeRepository.searchByKeyword(key)

        if (places.isEmpty()) {
            return PlaceSearchListResponse(places = emptyList())
        }

        val placeIds = places.mapNotNull { it.id }
        val recommendedIds = recommendRepository.findRecommendedPlaceIds(memberId).toSet()

        val recommendCountMap = recommendRepository.countRecommendedByPlaceIds(placeIds)
            .associate { it.placeId to it.recommendCount }

        return PlaceSearchListResponse(
            places = places.map { place ->
                val placeId = place.id!!
                PlaceSearchResponse(
                    latitude = place.latitude,
                    longitude = place.longitude,
                    placeId = placeId,
                    reviewCount = 0,
                    recommendCount = recommendCountMap[placeId] ?: 0L,
                    recommended = recommendedIds.contains(placeId)
                )
            }
        )
    }
}
