package com.teamharibo.goms.domain.place.service.impl

import com.teamharibo.goms.domain.place.dto.response.PlaceSearchListResponse
import com.teamharibo.goms.domain.place.dto.response.PlaceSearchResponse
import com.teamharibo.goms.domain.place.repository.PlaceRecommendRepository
import com.teamharibo.goms.domain.place.repository.PlaceRepository
import com.teamharibo.goms.domain.place.service.PlaceSearchService
import com.teamharibo.goms.domain.place.util.PlaceSummaryMapper
import com.teamharibo.goms.domain.review.repository.ReviewRepository
import com.teamharibo.goms.global.exception.ErrorCode
import com.teamharibo.goms.global.exception.GlobalException
import com.teamharibo.goms.global.util.MemberUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PlaceSearchServiceImpl(
    private val placeRepository: PlaceRepository,
    private val recommendRepository: PlaceRecommendRepository,
    private val reviewRepository: ReviewRepository,
    private val memberUtil: MemberUtil,
    private val placeSummaryMapper: PlaceSummaryMapper,
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

        val reviewCountMap = reviewRepository.countActiveByPlaceIds(placeIds)
            .associate { it.placeId to it.reviewCount }

        return PlaceSearchListResponse(
            places = places.map { place ->
                val placeId = place.id!!
                placeSummaryMapper.toSearchResponse(
                    place = place,
                    reviewCount = reviewCountMap[placeId] ?: 0L,
                    recommendCount = recommendCountMap[placeId] ?: 0L,
                    recommended = recommendedIds.contains(placeId),
                )
            }
        )
    }
}