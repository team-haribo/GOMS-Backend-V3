package com.teamharibo.goms.domain.review.service.impl

import com.teamharibo.goms.domain.place.exception.NotFoundPlaceException
import com.teamharibo.goms.domain.place.repository.PlaceRepository
import com.teamharibo.goms.domain.review.dto.request.ReviewCreateRequest
import com.teamharibo.goms.domain.review.dto.response.ReviewCreateResponse
import com.teamharibo.goms.domain.review.entity.Review
import com.teamharibo.goms.domain.review.exception.AlreadyReviewedPlaceException
import com.teamharibo.goms.domain.review.exception.ReviewContentEmptyException
import com.teamharibo.goms.domain.review.exception.ReviewContentTooLongException
import com.teamharibo.goms.domain.review.repository.ReviewRepository
import com.teamharibo.goms.domain.review.service.ReviewCreateService
import com.teamharibo.goms.global.log.LogFormat
import com.teamharibo.goms.global.util.MemberUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReviewCreateServiceImpl(
    private val placeRepository: PlaceRepository,
    private val reviewRepository: ReviewRepository,
    private val memberUtil: MemberUtil
) : ReviewCreateService {

    private val log = LoggerFactory.getLogger(ReviewCreateServiceImpl::class.java)

    @Transactional
    override fun create(placeId: Long, request: ReviewCreateRequest): ReviewCreateResponse {
        val content = request.content.trim()

        log.info(
            LogFormat.message(
                domain = "REVIEW",
                event = "리뷰 작성 시도",
                "placeId" to placeId,
                "contentLength" to content.length
            )
        )

        if (content.isBlank()) {
            log.warn(
                LogFormat.message(
                    domain = "REVIEW",
                    event = "리뷰 작성 실패",
                    "placeId" to placeId,
                    "reason" to "내용 없음"
                )
            )
            throw ReviewContentEmptyException()
        }

        if (content.length > 500) {
            log.warn(
                LogFormat.message(
                    domain = "REVIEW",
                    event = "리뷰 작성 실패",
                    "placeId" to placeId,
                    "reason" to "내용 길이 초과"
                )
            )
            throw ReviewContentTooLongException()
        }

        val place = placeRepository.findByIdAndIsActiveTrue(placeId).orElseThrow {
            log.warn(
                LogFormat.message(
                    domain = "REVIEW",
                    event = "리뷰 작성 실패",
                    "placeId" to placeId,
                    "reason" to "존재하지 않거나 비활성화된 장소"
                )
            )
            NotFoundPlaceException()
        }

        val member = memberUtil.currentMember()

        if (reviewRepository.existsByPlaceIdAndMemberIdAndDeletedAtIsNull(placeId, member.id!!)) {
            log.warn(
                LogFormat.message(
                    domain = "REVIEW",
                    event = "리뷰 작성 실패",
                    "memberId" to member.id,
                    "placeId" to placeId,
                    "reason" to "이미 리뷰 작성함"
                )
            )
            throw AlreadyReviewedPlaceException()
        }

        val saved = reviewRepository.save(
            Review(
                place = place,
                member = member,
                content = content
            )
        )

        log.info(
            LogFormat.message(
                domain = "REVIEW",
                event = "리뷰 작성 완료",
                "memberId" to member.id,
                "placeId" to placeId,
                "reviewId" to saved.id
            )
        )

        return ReviewCreateResponse(review_id = saved.id!!)
    }
}