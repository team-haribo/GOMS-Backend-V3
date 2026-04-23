package com.example.team.haribo.goms.domain.review.service.impl

import com.example.team.haribo.goms.domain.review.dto.response.MyReviewListResponse
import com.example.team.haribo.goms.domain.review.dto.response.MyReviewResponse
import com.example.team.haribo.goms.domain.review.repository.ReviewRepository
import com.example.team.haribo.goms.domain.review.service.ReviewMyListService
import com.example.team.haribo.goms.global.util.MemberUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReviewMyListServiceImpl(
    private val reviewRepository: ReviewRepository,
    private val memberUtil: MemberUtil
) : ReviewMyListService {

    @Transactional(readOnly = true)
    override fun getMyReviews(): MyReviewListResponse {
        val memberId = memberUtil.currentMemberId()
        val reviews = reviewRepository.findAllActiveByMemberId(memberId)

        return MyReviewListResponse(
            reviews = reviews.map {
                MyReviewResponse(
                    reviewId = it.id!!,
                    placeId = it.place.id!!,
                    placeName = it.place.placeName,
                    categoryName = it.place.categoryName,
                    address = it.place.address,
                    content = it.content,
                    reviewedAt = it.createdAt!!
                )
            }
        )
    }
}