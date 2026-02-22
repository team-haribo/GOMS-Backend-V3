package com.example.team.haribo.goms.domain.place.service.impl

import com.example.team.haribo.goms.domain.place.dto.response.PlaceSummaryResponse
import com.example.team.haribo.goms.domain.place.dto.response.PlacesResponse
import com.example.team.haribo.goms.domain.place.repository.PlaceRecommendRepository
import com.example.team.haribo.goms.domain.place.service.PlaceHotPlaceService
import com.example.team.haribo.goms.global.util.MemberUtil
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PlaceHotPlaceServiceImpl(
    private val recommendRepository: PlaceRecommendRepository,
    private val memberUtil: MemberUtil
) : PlaceHotPlaceService {

    @Transactional(readOnly = true)
    override fun getHotPlaces(): PlacesResponse {
        val memberId = memberUtil.currentMemberId()
        val recommendedIds = recommendRepository.findRecommendedPlaceIds(memberId).toSet()

        val hotIds = recommendRepository.findHotPlaceIds(PageRequest.of(0, 20))
        if (hotIds.isEmpty()) {
            return PlacesResponse(places = emptyList())
        }

        val recommendCountMap = recommendRepository.countRecommendedByPlaceIds(hotIds)
            .associate { it.placeId to it.recommendCount }

        return PlacesResponse(
            places = hotIds.map { placeId ->
                PlaceSummaryResponse(
                    placeId = placeId,
                    reviewCount = 0,
                    recommendCount = recommendCountMap[placeId] ?: 0L,
                    recommended = recommendedIds.contains(placeId)
                )
            }
        )
    }
}
