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
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
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
    fun upsert(@RequestBody request: PlaceUpsertRequest): ResponseEntity<PlaceUpsertResponse> {
        return ResponseEntity.status(201).body(upsertService.upsert(request))
    }

    @GetMapping("/hot-place")
    fun hotPlace(@RequestParam("days", required = false) days: Long?): ResponseEntity<PlacesResponse> {
        return ResponseEntity.ok(hotPlaceService.getHotPlaces(days))
    }

    @GetMapping("/{placeId}")
    fun detail(@PathVariable placeId: Long): ResponseEntity<PlaceDetailResponse> {
        return ResponseEntity.ok(detailService.getDetail(placeId))
    }

    @GetMapping("/search")
    fun search(@RequestParam("keyword", required = false) keyword: String?): ResponseEntity<PlaceSearchListResponse> {
        return ResponseEntity.ok(searchService.search(keyword))
    }

    @PostMapping("/recommend/{placeId}")
    fun recommend(@PathVariable placeId: Long): ResponseEntity<RecommendResponse> {
        return ResponseEntity.ok(recommendService.recommend(placeId))
    }

    @DeleteMapping("/recommend/{placeId}")
    fun unrecommend(@PathVariable placeId: Long): ResponseEntity<RecommendResponse> {
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
    fun getPlaceReviews(@PathVariable placeId: Long): ResponseEntity<PlaceReviewListResponse> {
        return ResponseEntity.ok(reviewGetService.getPlaceReviews(placeId))
    }

    @GetMapping("/review/count/{placeId}")
    fun countPlaceReviews(@PathVariable placeId: Long): ResponseEntity<ReviewCountResponse> {
        return ResponseEntity.ok(reviewGetService.countPlaceReviews(placeId))
    }
}
