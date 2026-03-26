package com.example.team.haribo.goms.domain.place.controller

import com.example.team.haribo.goms.domain.place.dto.request.PlaceUpsertRequest
import com.example.team.haribo.goms.domain.place.dto.response.PlaceDetailResponse
import com.example.team.haribo.goms.domain.place.dto.response.PlaceSearchListResponse
import com.example.team.haribo.goms.domain.place.dto.response.PlaceUpsertResponse
import com.example.team.haribo.goms.domain.place.dto.response.PlacesResponse
import com.example.team.haribo.goms.domain.place.dto.response.RecommendCountResponse
import com.example.team.haribo.goms.domain.place.dto.response.RecommendResponse
import com.example.team.haribo.goms.domain.place.service.PlaceDetailService
import com.example.team.haribo.goms.domain.place.service.PlaceHotPlaceService
import com.example.team.haribo.goms.domain.place.service.PlaceRecommendService
import com.example.team.haribo.goms.domain.place.service.PlaceSearchService
import com.example.team.haribo.goms.domain.place.service.PlaceUpsertService
import com.example.team.haribo.goms.domain.review.dto.response.PlaceReviewListResponse
import com.example.team.haribo.goms.domain.review.dto.response.ReviewCountResponse
import com.example.team.haribo.goms.domain.review.service.ReviewGetService
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@Validated
@RequestMapping("/api/v3/place")
class PlaceController(
    private val upsertService: PlaceUpsertService,
    private val hotPlaceService: PlaceHotPlaceService,
    private val detailService: PlaceDetailService,
    private val searchService: PlaceSearchService,
    private val recommendService: PlaceRecommendService,
    private val reviewGetService: ReviewGetService
) {

    @PostMapping("/upsert")
    fun upsert(@Valid @RequestBody request: PlaceUpsertRequest): ResponseEntity<PlaceUpsertResponse> {
        return ResponseEntity.status(201).body(upsertService.upsert(request))
    }

    @GetMapping("/hot-place")
    fun hotPlace(
        @RequestParam("days", required = false)
        @Positive(message = "days 는 1 이상이어야 합니다.")
        days: Long?
    ): ResponseEntity<PlacesResponse> {
        return ResponseEntity.ok(hotPlaceService.getHotPlaces(days))
    }

    @GetMapping("/{placeId}")
    fun detail(
        @PathVariable
        @Positive(message = "placeId 는 1 이상이어야 합니다.")
        placeId: Long
    ): ResponseEntity<PlaceDetailResponse> {
        return ResponseEntity.ok(detailService.getDetail(placeId))
    }

    @GetMapping("/search")
    fun search(
        @RequestParam("keyword")
        @NotBlank(message = "keyword 는 비어 있을 수 없습니다.")
        keyword: String
    ): ResponseEntity<PlaceSearchListResponse> {
        return ResponseEntity.ok(searchService.search(keyword))
    }

    @PostMapping("/recommend/{placeId}")
    fun recommend(
        @PathVariable
        @Positive(message = "placeId 는 1 이상이어야 합니다.")
        placeId: Long
    ): ResponseEntity<RecommendResponse> {
        return ResponseEntity.ok(recommendService.recommend(placeId))
    }

    @DeleteMapping("/recommend/{placeId}")
    fun unrecommend(
        @PathVariable
        @Positive(message = "placeId 는 1 이상이어야 합니다.")
        placeId: Long
    ): ResponseEntity<RecommendResponse> {
        return ResponseEntity.ok(recommendService.unrecommend(placeId))
    }

    @GetMapping("/recommended")
    fun recommended(): ResponseEntity<PlacesResponse> {
        return ResponseEntity.ok(recommendService.getRecommendedPlaces())
    }

    @GetMapping("/recommended/count")
    fun recommendedCount(): ResponseEntity<RecommendCountResponse> {
        return ResponseEntity.ok(recommendService.getRecommendedCount())
    }

    @GetMapping("/review/{placeId}")
    fun getPlaceReviews(
        @PathVariable
        @Positive(message = "placeId 는 1 이상이어야 합니다.")
        placeId: Long
    ): ResponseEntity<PlaceReviewListResponse> {
        return ResponseEntity.ok(reviewGetService.getPlaceReviews(placeId))
    }

    @GetMapping("/review/count/{placeId}")
    fun countPlaceReviews(
        @PathVariable
        @Positive(message = "placeId 는 1 이상이어야 합니다.")
        placeId: Long
    ): ResponseEntity<ReviewCountResponse> {
        return ResponseEntity.ok(reviewGetService.countPlaceReviews(placeId))
    }
}