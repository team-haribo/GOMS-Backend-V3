package com.example.team.haribo.goms.domain.place.controller

import com.example.team.haribo.goms.domain.place.dto.request.PlaceUpsertRequest
import com.example.team.haribo.goms.domain.place.dto.response.*
import com.example.team.haribo.goms.domain.place.service.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v3/place")
class PlaceController(
    private val upsertService: PlaceUpsertService,
    private val hotPlaceService: PlaceHotPlaceService,
    private val detailService: PlaceDetailService,
    private val searchService: PlaceSearchService,
    private val recommendService: PlaceRecommendService
) {

    @PostMapping
    fun upsert(@RequestBody request: PlaceUpsertRequest): ResponseEntity<PlaceUpsertResponse> {
        return ResponseEntity.status(201).body(upsertService.upsert(request))
    }

    @GetMapping("/hot-place")
    fun hotPlace(): ResponseEntity<PlacesResponse> {
        return ResponseEntity.ok(hotPlaceService.getHotPlaces())
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
}
