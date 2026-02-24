package com.example.team.haribo.goms.domain.review.controller

import com.example.team.haribo.goms.domain.review.dto.request.ReviewCreateRequest
import com.example.team.haribo.goms.domain.review.dto.response.ReviewCreateResponse
import com.example.team.haribo.goms.domain.review.service.ReviewCreateService
import com.example.team.haribo.goms.domain.review.service.ReviewDeleteService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v3/review")
class ReviewController(
    private val reviewCreateService: ReviewCreateService,
    private val reviewDeleteService: ReviewDeleteService
) {

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
