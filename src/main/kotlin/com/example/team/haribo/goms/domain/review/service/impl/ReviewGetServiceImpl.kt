package com.example.team.haribo.goms.domain.review.service.impl

import com.example.team.haribo.goms.domain.place.exception.NotFoundPlaceException
import com.example.team.haribo.goms.domain.place.repository.PlaceRepository
import com.example.team.haribo.goms.domain.review.dto.response.PlaceReviewListResponse
import com.example.team.haribo.goms.domain.review.dto.response.ReviewCountResponse
import com.example.team.haribo.goms.domain.review.dto.response.ReviewResponse
import com.example.team.haribo.goms.domain.review.repository.ReviewRepository
import com.example.team.haribo.goms.domain.review.service.ReviewGetService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReviewGetServiceImpl(
    private val placeRepository: PlaceRepository,
    private val reviewRepository: ReviewRepository
) : ReviewGetService {

    @Transactional(readOnly = true)
    override fun getPlaceReviews(placeId: Long): PlaceReviewListResponse {
        if (!placeRepository.existsById(placeId)) throw NotFoundPlaceException()

        val reviews = reviewRepository.findAllActiveByPlaceId(placeId)

        return PlaceReviewListResponse(
            reviews = reviews.map {
                ReviewResponse(
                    review_id = it.id!!,
                    name = it.member.name,
                    grade = it.member.grade,
                    department = it.member.department,
                    content = it.content,
                    reviewed_at = it.createdAt!!
                )
            }
        )
    }

    @Transactional(readOnly = true)
    override fun countPlaceReviews(placeId: Long): ReviewCountResponse {
        if (!placeRepository.existsById(placeId)) throw NotFoundPlaceException()
        return ReviewCountResponse(review_count = reviewRepository.countActiveByPlaceId(placeId))
    }
}
