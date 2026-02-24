package com.example.team.haribo.goms.domain.review.service.impl

import com.example.team.haribo.goms.domain.place.exception.NotFoundPlaceException
import com.example.team.haribo.goms.domain.place.repository.PlaceRepository
import com.example.team.haribo.goms.domain.review.dto.request.ReviewCreateRequest
import com.example.team.haribo.goms.domain.review.dto.response.ReviewCreateResponse
import com.example.team.haribo.goms.domain.review.entity.Review
import com.example.team.haribo.goms.domain.review.exception.AlreadyReviewedPlaceException
import com.example.team.haribo.goms.domain.review.exception.ReviewContentEmptyException
import com.example.team.haribo.goms.domain.review.exception.ReviewContentTooLongException
import com.example.team.haribo.goms.domain.review.repository.ReviewRepository
import com.example.team.haribo.goms.domain.review.service.ReviewCreateService
import com.example.team.haribo.goms.global.util.MemberUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReviewCreateServiceImpl(
    private val placeRepository: PlaceRepository,
    private val reviewRepository: ReviewRepository,
    private val memberUtil: MemberUtil
) : ReviewCreateService {

    @Transactional
    override fun create(placeId: Long, request: ReviewCreateRequest): ReviewCreateResponse {
        val content = request.content.trim()

        if (content.isBlank()) throw ReviewContentEmptyException()
        if (content.length > 500) throw ReviewContentTooLongException()

        val place = placeRepository.findById(placeId).orElseThrow { NotFoundPlaceException() }
        val member = memberUtil.currentMember()

        if (reviewRepository.existsByPlaceIdAndMemberIdAndDeletedAtIsNull(placeId, member.id!!)) {
            throw AlreadyReviewedPlaceException()
        }

        val saved = reviewRepository.save(
            Review(
                place = place,
                member = member,
                content = content
            )
        )

        return ReviewCreateResponse(review_id = saved.id!!)
    }
}
