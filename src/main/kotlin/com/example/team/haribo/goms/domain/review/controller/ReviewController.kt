package com.example.team.haribo.goms.domain.review.controller

import com.example.team.haribo.goms.domain.review.dto.request.ReviewCreateRequest
import com.example.team.haribo.goms.domain.review.dto.response.ReviewCreateResponse
import com.example.team.haribo.goms.domain.review.service.ReviewCreateService
import com.example.team.haribo.goms.domain.review.service.ReviewDeleteService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Review", description = "리뷰 관련 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@Validated
@RequestMapping("/api/v3/review")
class ReviewController(
    private val reviewCreateService: ReviewCreateService,
    private val reviewDeleteService: ReviewDeleteService
) {

    @Operation(
        summary = "리뷰 작성",
        description = "특정 장소에 리뷰를 작성합니다.",
        parameters = [
            Parameter(
                name = "placeId",
                description = "리뷰를 작성할 장소 ID",
                required = true,
                example = "1"
            )
        ],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [
                Content(
                    schema = Schema(implementation = ReviewCreateRequest::class)
                )
            ]
        ),
        responses = [
            ApiResponse(responseCode = "201", description = "리뷰 작성 성공")
        ]
    )
    @PostMapping("/{placeId}")
    fun create(
        @PathVariable
        @Positive(message = "placeId 는 1 이상이어야 합니다.")
        placeId: Long,
        @Valid @RequestBody request: ReviewCreateRequest
    ): ResponseEntity<ReviewCreateResponse> {
        return ResponseEntity.status(201).body(reviewCreateService.create(placeId, request))
    }

    @Operation(
        summary = "리뷰 삭제",
        description = "작성한 리뷰를 삭제합니다.",
        parameters = [
            Parameter(
                name = "reviewId",
                description = "삭제할 리뷰 ID",
                required = true,
                example = "1"
            )
        ],
        responses = [
            ApiResponse(responseCode = "204", description = "리뷰 삭제 성공")
        ]
    )
    @DeleteMapping("/{reviewId}")
    fun delete(
        @PathVariable
        @Positive(message = "reviewId 는 1 이상이어야 합니다.")
        reviewId: Long
    ): ResponseEntity<Void> {
        reviewDeleteService.delete(reviewId)
        return ResponseEntity.noContent().build()
    }
}