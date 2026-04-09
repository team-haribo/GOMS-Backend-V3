package com.example.team.haribo.goms.domain.place.controller

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
import com.example.team.haribo.goms.domain.review.dto.response.PlaceReviewListResponse
import com.example.team.haribo.goms.domain.review.dto.response.ReviewCountResponse
import com.example.team.haribo.goms.domain.review.service.ReviewGetService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Place", description = "장소 관련 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@Validated
@RequestMapping("/api/v3/place")
class PlaceController(
    private val hotPlaceService: PlaceHotPlaceService,
    private val detailService: PlaceDetailService,
    private val searchService: PlaceSearchService,
    private val recommendService: PlaceRecommendService,
    private val reviewGetService: ReviewGetService
) {

    @Operation(
        summary = "핫플레이스 조회",
        description = "최근 추천 데이터를 기준으로 인기 장소를 조회합니다.",
        parameters = [
            Parameter(
                name = "days",
                description = "최근 N일 기준 조회",
                required = false,
                example = "7"
            )
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "조회 성공")
        ]
    )
    @GetMapping("/hot-place")
    fun hotPlace(
        @RequestParam("days", required = false)
        @Positive(message = "days 는 1 이상이어야 합니다.")
        days: Long?
    ): ResponseEntity<PlacesResponse> {
        return ResponseEntity.ok(hotPlaceService.getHotPlaces(days))
    }

    @Operation(
        summary = "장소 상세 조회",
        description = "placeId 기준으로 장소 상세 정보를 조회합니다.",
        parameters = [
            Parameter(
                name = "placeId",
                description = "장소 ID",
                required = true,
                example = "1"
            )
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "조회 성공")
        ]
    )
    @GetMapping("/{placeId}")
    fun detail(
        @PathVariable
        @Positive(message = "placeId 는 1 이상이어야 합니다.")
        placeId: Long
    ): ResponseEntity<PlaceDetailResponse> {
        return ResponseEntity.ok(detailService.getDetail(placeId))
    }

    @Operation(
        summary = "장소 검색",
        description = "키워드로 장소명을 검색합니다.",
        parameters = [
            Parameter(
                name = "keyword",
                description = "검색 키워드",
                required = true,
                example = "편의점"
            )
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "검색 성공")
        ]
    )
    @GetMapping("/search")
    fun search(
        @RequestParam("keyword")
        @NotBlank(message = "keyword 는 비어 있을 수 없습니다.")
        keyword: String
    ): ResponseEntity<PlaceSearchListResponse> {
        return ResponseEntity.ok(searchService.search(keyword))
    }

    @Operation(
        summary = "장소 추천",
        description = "특정 장소를 추천합니다.",
        parameters = [
            Parameter(
                name = "placeId",
                description = "장소 ID",
                required = true,
                example = "1"
            )
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "추천 성공")
        ]
    )
    @PostMapping("/recommend/{placeId}")
    fun recommend(
        @PathVariable
        @Positive(message = "placeId 는 1 이상이어야 합니다.")
        placeId: Long
    ): ResponseEntity<RecommendResponse> {
        return ResponseEntity.ok(recommendService.recommend(placeId))
    }

    @Operation(
        summary = "장소 추천 취소",
        description = "특정 장소 추천을 취소합니다.",
        parameters = [
            Parameter(
                name = "placeId",
                description = "장소 ID",
                required = true,
                example = "1"
            )
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "추천 취소 성공")
        ]
    )
    @DeleteMapping("/recommend/{placeId}")
    fun unrecommend(
        @PathVariable
        @Positive(message = "placeId 는 1 이상이어야 합니다.")
        placeId: Long
    ): ResponseEntity<RecommendResponse> {
        return ResponseEntity.ok(recommendService.unrecommend(placeId))
    }

    @Operation(
        summary = "내가 추천한 장소 목록 조회",
        description = "현재 로그인한 사용자가 추천한 장소 목록을 조회합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "조회 성공")
        ]
    )
    @GetMapping("/recommended")
    fun recommended(): ResponseEntity<PlacesResponse> {
        return ResponseEntity.ok(recommendService.getRecommendedPlaces())
    }

    @Operation(
        summary = "내 추천 장소 개수 조회",
        description = "현재 로그인한 사용자가 추천한 장소 개수를 조회합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "조회 성공")
        ]
    )
    @GetMapping("/recommended/count")
    fun recommendedCount(): ResponseEntity<RecommendCountResponse> {
        return ResponseEntity.ok(recommendService.getRecommendedCount())
    }

    @Operation(
        summary = "장소 리뷰 목록 조회",
        description = "특정 장소의 리뷰 목록을 조회합니다.",
        parameters = [
            Parameter(
                name = "placeId",
                description = "장소 ID",
                required = true,
                example = "1"
            )
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "조회 성공")
        ]
    )
    @GetMapping("/review/{placeId}")
    fun getPlaceReviews(
        @PathVariable
        @Positive(message = "placeId 는 1 이상이어야 합니다.")
        placeId: Long
    ): ResponseEntity<PlaceReviewListResponse> {
        return ResponseEntity.ok(reviewGetService.getPlaceReviews(placeId))
    }

    @Operation(
        summary = "장소 리뷰 개수 조회",
        description = "특정 장소의 리뷰 개수를 조회합니다.",
        parameters = [
            Parameter(
                name = "placeId",
                description = "장소 ID",
                required = true,
                example = "1"
            )
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "조회 성공")
        ]
    )
    @GetMapping("/review/count/{placeId}")
    fun countPlaceReviews(
        @PathVariable
        @Positive(message = "placeId 는 1 이상이어야 합니다.")
        placeId: Long
    ): ResponseEntity<ReviewCountResponse> {
        return ResponseEntity.ok(reviewGetService.countPlaceReviews(placeId))
    }
}