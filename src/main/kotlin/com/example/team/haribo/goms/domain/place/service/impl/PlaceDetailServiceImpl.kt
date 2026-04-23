package com.example.team.haribo.goms.domain.place.service.impl

import com.example.team.haribo.goms.domain.place.dto.response.PlaceDetailResponse
import com.example.team.haribo.goms.domain.place.exception.NotFoundPlaceException
import com.example.team.haribo.goms.domain.place.repository.PlaceRecommendRepository
import com.example.team.haribo.goms.domain.place.repository.PlaceRepository
import com.example.team.haribo.goms.domain.place.service.PlaceDetailService
import com.example.team.haribo.goms.domain.review.repository.ReviewRepository
import com.example.team.haribo.goms.global.util.MemberUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PlaceDetailServiceImpl(
    private val placeRepository: PlaceRepository,
    private val recommendRepository: PlaceRecommendRepository,
    private val reviewRepository: ReviewRepository,
    private val memberUtil: MemberUtil
) : PlaceDetailService {

    @Transactional(readOnly = true)
    override fun getDetail(placeId: Long): PlaceDetailResponse {
        val memberId = memberUtil.currentMemberId()
        val place = placeRepository.findByIdAndIsActiveTrue(placeId).orElseThrow { NotFoundPlaceException() }

        val recommendCount = recommendRepository.countByPlaceIdAndRecommendedTrue(placeId)
        val reviewCount = reviewRepository.countActiveByPlaceId(placeId)
        val recommended = recommendRepository.findByPlaceIdAndMemberId(placeId, memberId)
            .map { it.recommended }
            .orElse(false)

        return PlaceDetailResponse(
            placeId = place.id!!,
            placeName = place.placeName,
            address = place.address,
            roadAddress = place.roadAddress,
            latitude = place.latitude,
            longitude = place.longitude,
            categoryGroupName = place.categoryGroupName,
            categoryName = place.categoryName,
            phone = place.phone,
            placeUrl = place.placeUrl,
            reviewCount = reviewCount,
            recommendCount = recommendCount,
            recommended = recommended
        )
    }
}