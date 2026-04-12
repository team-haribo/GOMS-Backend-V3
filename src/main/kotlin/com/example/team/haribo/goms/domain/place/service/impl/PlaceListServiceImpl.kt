package com.example.team.haribo.goms.domain.place.service.impl

import com.example.team.haribo.goms.domain.place.dto.response.PlaceSummaryResponse
import com.example.team.haribo.goms.domain.place.dto.response.PlacesResponse
import com.example.team.haribo.goms.domain.place.repository.PlaceRecommendRepository
import com.example.team.haribo.goms.domain.place.repository.PlaceRepository
import com.example.team.haribo.goms.domain.place.service.PlaceListService
import com.example.team.haribo.goms.domain.review.repository.ReviewRepository
import com.example.team.haribo.goms.global.util.MemberUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PlaceListServiceImpl(
    private val placeRepository: PlaceRepository,
    private val recommendRepository: PlaceRecommendRepository,
    private val reviewRepository: ReviewRepository,
    private val memberUtil: MemberUtil
) : PlaceListService {

    @Transactional(readOnly = true)
    override fun getPlaces(): PlacesResponse {
        val memberId = memberUtil.currentMemberId()
        val places = placeRepository.findAllByIsActiveTrue()

        if (places.isEmpty()) {
            return PlacesResponse(places = emptyList())
        }

        val placeIds = places.mapNotNull { it.id }
        val recommendedIds = recommendRepository.findRecommendedPlaceIds(memberId).toSet()

        val recommendCountMap = recommendRepository.countRecommendedByPlaceIds(placeIds)
            .associate { it.placeId to it.recommendCount }

        val reviewCountMap = reviewRepository.countActiveByPlaceIds(placeIds)
            .associate { it.placeId to it.reviewCount }

        return PlacesResponse(
            places = places.map { place ->
                val placeId = place.id!!
                PlaceSummaryResponse(
                    placeId = placeId,
                    placeName = place.placeName,
                    address = place.address,
                    roadAddress = place.roadAddress,
                    latitude = place.latitude,
                    longitude = place.longitude,
                    categoryGroupName = place.categoryGroupName,
                    categoryName = place.categoryName,
                    reviewCount = reviewCountMap[placeId] ?: 0L,
                    recommendCount = recommendCountMap[placeId] ?: 0L,
                    recommended = recommendedIds.contains(placeId)
                )
            }
        )
    }
}