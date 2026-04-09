package com.example.team.haribo.goms.domain.place.service.impl

import com.example.team.haribo.goms.domain.place.dto.response.PlaceSummaryResponse
import com.example.team.haribo.goms.domain.place.dto.response.PlacesResponse
import com.example.team.haribo.goms.domain.place.repository.PlaceRecommendRepository
import com.example.team.haribo.goms.domain.place.repository.PlaceRepository
import com.example.team.haribo.goms.domain.place.service.PlaceHotPlaceService
import com.example.team.haribo.goms.domain.review.repository.ReviewRepository
import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException
import com.example.team.haribo.goms.global.util.MemberUtil
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PlaceHotPlaceServiceImpl(
    private val placeRepository: PlaceRepository,
    private val recommendRepository: PlaceRecommendRepository,
    private val reviewRepository: ReviewRepository,
    private val memberUtil: MemberUtil
) : PlaceHotPlaceService {

    @Transactional(readOnly = true)
    override fun getHotPlaces(days: Long?): PlacesResponse {
        val d = (days ?: 3L).also {
            if (it <= 0L || it > 30L) throw GlobalException(ErrorCode.INVALID_REQUEST)
        }

        val memberId = memberUtil.currentMemberId()
        val recommendedIds = recommendRepository.findRecommendedPlaceIds(memberId).toSet()
        val since = LocalDateTime.now().minusDays(d)

        val hotIds = recommendRepository.findHotPlaceIdsSince(
            since,
            PageRequest.of(0, 3)
        )

        if (hotIds.isEmpty()) {
            return PlacesResponse(places = emptyList())
        }

        val placeMap = placeRepository.findAllById(hotIds)
            .filter { it.id != null && it.isActive }
            .associateBy { it.id!! }

        val recommendCountMap = recommendRepository
            .countRecommendedByPlaceIdsSince(hotIds, since)
            .associate { it.placeId to it.recommendCount }

        val reviewCountMap = reviewRepository.countActiveByPlaceIds(hotIds)
            .associate { it.placeId to it.reviewCount }

        return PlacesResponse(
            places = hotIds.mapNotNull { placeId ->
                val place = placeMap[placeId] ?: return@mapNotNull null
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