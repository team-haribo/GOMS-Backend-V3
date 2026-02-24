package com.example.team.haribo.goms.domain.review.controller

import com.example.team.haribo.goms.domain.review.dto.request.ReviewCreateRequest
import com.example.team.haribo.goms.domain.review.dto.response.PlaceReviewListResponse
import com.example.team.haribo.goms.domain.review.dto.response.ReviewCountResponse
import com.example.team.haribo.goms.domain.review.dto.response.ReviewCreateResponse
import com.example.team.haribo.goms.domain.review.service.ReviewCreateService
import com.example.team.haribo.goms.domain.review.service.ReviewDeleteService
import com.example.team.haribo.goms.domain.review.service.ReviewGetService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v3/place/review")
class ReviewController(
    private val reviewCreateService: ReviewCreateService,
    private val reviewGetService: ReviewGetService,
    private val reviewDeleteService: ReviewDeleteService
) {

    @GetMapping("/{placeId}")
    fun getPlaceReviews(@PathVariable placeId: Long): ResponseEntity<PlaceReviewListResponse> {
        return ResponseEntity.ok(reviewGetService.getPlaceReviews(placeId))
    }

    @GetMapping("/count/{placeId}")
    fun countPlaceReviews(@PathVariable placeId: Long): ResponseEntity<ReviewCountResponse> {
        return ResponseEntity.ok(reviewGetService.countPlaceReviews(placeId))
    }

    @PostMapping("/{placeId}")
    fun create(
        @PathVariable placeId: Long,
        @RequestBody request: ReviewCreateRequest
    ): ResponseEntity<ReviewCreateResponse> {
        return ResponseEntity.status(201).body(reviewCreateService.create(placeId, request))
    }

    @DeleteMapping("/{reviewId}")
    fun delete(@PathVariable reviewId: Long): ResponseEntity<Void> {
        reviewDeleteService.delete(reviewId)
        return ResponseEntity.noContent().build()
    }
}
