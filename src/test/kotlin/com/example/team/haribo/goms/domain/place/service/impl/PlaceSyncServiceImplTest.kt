package com.example.team.haribo.goms.domain.place.service.impl

import com.example.team.haribo.goms.domain.place.client.KakaoPlaceClient
import com.example.team.haribo.goms.domain.place.dto.response.KakaoPlaceDocument
import com.example.team.haribo.goms.domain.place.dto.response.KakaoPlaceMeta
import com.example.team.haribo.goms.domain.place.dto.response.KakaoPlaceSearchResponse
import com.example.team.haribo.goms.domain.place.entity.Place
import com.example.team.haribo.goms.domain.place.repository.PlaceRepository
import com.example.team.haribo.goms.domain.place.util.PlaceDistanceCalculator
import com.example.team.haribo.goms.domain.place.util.PlaceSearchPointGenerator
import com.example.team.haribo.goms.domain.place.util.PlaceSyncFilter
import com.example.team.haribo.goms.domain.place.util.SearchPoint
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime
import java.util.Optional

class PlaceSyncServiceImplTest : DescribeSpec({

    val kakaoPlaceClient = mockk<KakaoPlaceClient>()
    val placeRepository = mockk<PlaceRepository>()
    val placeSyncFilter = mockk<PlaceSyncFilter>()
    val placeSearchPointGenerator = mockk<PlaceSearchPointGenerator>()
    val placeDistanceCalculator = mockk<PlaceDistanceCalculator>()

    val service = PlaceSyncServiceImpl(
        kakaoPlaceClient,
        placeRepository,
        placeSyncFilter,
        placeSearchPointGenerator,
        placeDistanceCalculator
    )

    val point = SearchPoint(
        longitude = 126.8007,
        latitude = 35.1427,
        radius = 350
    )

    val document = KakaoPlaceDocument(
        id = "kakao-place-1",
        place_name = "테스트 식당",
        category_name = "음식점 > 중식",
        category_group_code = "FD6",
        category_group_name = "음식점",
        phone = "010-1234-5678",
        address_name = "광주 광산구 테스트로 1",
        road_address_name = "광주 광산구 테스트로 1",
        x = "126.8008",
        y = "35.1428",
        place_url = "https://place.map.kakao.com/1"
    )

    val response = KakaoPlaceSearchResponse(
        meta = KakaoPlaceMeta(
            total_count = 1,
            pageable_count = 1,
            is_end = true
        ),
        documents = listOf(document)
    )

    describe("PlaceSyncService") {

        context("Given: 새 장소가 수집됨") {
            every {
                placeSearchPointGenerator.generate(
                    centerLatitude = any(),
                    centerLongitude = any(),
                    offsetMeter = any(),
                    searchRadius = any()
                )
            } returns listOf(point)

            every {
                kakaoPlaceClient.searchByCategory(
                    categoryGroupCode = any(),
                    x = point.longitude.toString(),
                    y = point.latitude.toString(),
                    radius = point.radius,
                    page = 1,
                    size = 15
                )
            } returns response

            every { placeSyncFilter.isAllowed(document) } returns true
            every {
                placeDistanceCalculator.isWithinRadius(
                    originLatitude = any(),
                    originLongitude = any(),
                    targetLatitude = document.y.toDouble(),
                    targetLongitude = document.x.toDouble(),
                    radiusMeter = any()
                )
            } returns true

            every { placeRepository.findByExternalPlaceId(document.id) } returns Optional.empty()
            every { placeRepository.save(any()) } answers { firstArg() }
            every { placeRepository.findAllByLastSyncedAtBeforeAndIsActiveTrue(any()) } returns emptyList()

            it("When: 장소 동기화 시 Then: 새 장소가 생성된다") {
                val result = service.sync()

                result.createdCount shouldBe 1
                result.updatedCount shouldBe 0
                result.deactivatedCount shouldBe 0
                result.totalFetchedCount shouldBe 1
                result.searchPointCount shouldBe 1
                result.rawCollectedCount shouldBe 8

                verify(exactly = 1) { placeRepository.save(any()) }
            }
        }

        context("Given: 기존 장소 갱신과 비활성화 대상이 있음") {
            val existing = Place(
                id = 1L,
                externalPlaceId = document.id,
                placeName = "기존 장소명",
                address = "기존 주소",
                roadAddress = null,
                latitude = 35.0,
                longitude = 126.0,
                categoryGroupCode = "FD6",
                categoryGroupName = "음식점",
                categoryName = "기존 카테고리",
                phone = null,
                placeUrl = null,
                isActive = true,
                lastSyncedAt = LocalDateTime.now().minusDays(1),
                createdAt = LocalDateTime.now().minusDays(10),
                updatedAt = LocalDateTime.now().minusDays(1)
            )

            val stale = Place(
                id = 2L,
                externalPlaceId = "stale-place",
                placeName = "오래된 장소",
                address = "광주",
                roadAddress = null,
                latitude = 35.1,
                longitude = 126.1,
                categoryGroupCode = "FD6",
                categoryGroupName = "음식점",
                categoryName = "한식",
                phone = null,
                placeUrl = null,
                isActive = true,
                lastSyncedAt = LocalDateTime.now().minusDays(30),
                createdAt = LocalDateTime.now().minusDays(50),
                updatedAt = LocalDateTime.now().minusDays(30)
            )

            every {
                placeSearchPointGenerator.generate(
                    centerLatitude = any(),
                    centerLongitude = any(),
                    offsetMeter = any(),
                    searchRadius = any()
                )
            } returns listOf(point)

            every {
                kakaoPlaceClient.searchByCategory(
                    categoryGroupCode = any(),
                    x = point.longitude.toString(),
                    y = point.latitude.toString(),
                    radius = point.radius,
                    page = 1,
                    size = 15
                )
            } returns response

            every { placeSyncFilter.isAllowed(document) } returns true
            every {
                placeDistanceCalculator.isWithinRadius(
                    originLatitude = any(),
                    originLongitude = any(),
                    targetLatitude = document.y.toDouble(),
                    targetLongitude = document.x.toDouble(),
                    radiusMeter = any()
                )
            } returns true

            every { placeRepository.findByExternalPlaceId(document.id) } returns Optional.of(existing)
            every { placeRepository.findAllByLastSyncedAtBeforeAndIsActiveTrue(any()) } returns listOf(stale)

            it("When: 장소 동기화 시 Then: 기존 장소가 갱신되고 오래된 장소는 비활성화된다") {
                val result = service.sync()

                result.createdCount shouldBe 0
                result.updatedCount shouldBe 1
                result.deactivatedCount shouldBe 1
                result.totalFetchedCount shouldBe 1

                existing.placeName shouldBe "테스트 식당"
                existing.address shouldBe "광주 광산구 테스트로 1"
                stale.isActive shouldBe false
            }
        }
    }
})