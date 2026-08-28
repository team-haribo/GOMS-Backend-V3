package com.example.team.haribo.goms.domain.place.service.impl

import com.example.team.haribo.goms.domain.place.client.KakaoPlaceClient
import com.example.team.haribo.goms.domain.place.dto.response.KakaoPlaceDocument
import com.example.team.haribo.goms.domain.place.dto.response.KakaoPlaceMeta
import com.example.team.haribo.goms.domain.place.dto.response.KakaoPlaceSearchResponse
import com.example.team.haribo.goms.domain.place.service.PlaceSyncWriteResult
import com.example.team.haribo.goms.domain.place.service.PlaceSyncWriter
import com.example.team.haribo.goms.domain.place.util.PlaceDistanceCalculator
import com.example.team.haribo.goms.domain.place.util.PlaceSearchPointGenerator
import com.example.team.haribo.goms.domain.place.util.PlaceSyncFilter
import com.example.team.haribo.goms.domain.place.util.SearchPoint
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify

class PlaceSyncServiceImplTest : DescribeSpec({

    val kakaoPlaceClient = mockk<KakaoPlaceClient>()
    val placeSyncWriter = mockk<PlaceSyncWriter>()
    val placeSyncFilter = mockk<PlaceSyncFilter>()
    val placeSearchPointGenerator = mockk<PlaceSearchPointGenerator>()
    val placeDistanceCalculator = mockk<PlaceDistanceCalculator>()

    val service = PlaceSyncServiceImpl(
        kakaoPlaceClient,
        placeSyncWriter,
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

            val documentsSlot = slot<List<KakaoPlaceDocument>>()
            every { placeSyncWriter.write(capture(documentsSlot), any()) } returns
                PlaceSyncWriteResult(createdCount = 1, updatedCount = 0, deactivatedCount = 0)

            it("When: 장소 동기화 시 Then: 수집된 문서로 DB 반영을 위임하고 결과를 집계한다") {
                val result = service.sync()

                result.createdCount shouldBe 1
                result.updatedCount shouldBe 0
                result.deactivatedCount shouldBe 0
                result.totalFetchedCount shouldBe 1
                result.searchPointCount shouldBe 1
                result.rawCollectedCount shouldBe 8

                documentsSlot.captured shouldBe listOf(document)
                verify(exactly = 1) { placeSyncWriter.write(any(), any()) }
            }
        }

        context("Given: 필터/반경 조건에 걸려 제외되는 문서가 있음") {
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

            every { placeSyncFilter.isAllowed(document) } returns false

            val documentsSlot = slot<List<KakaoPlaceDocument>>()
            every { placeSyncWriter.write(capture(documentsSlot), any()) } returns
                PlaceSyncWriteResult(createdCount = 0, updatedCount = 0, deactivatedCount = 0)

            it("When: 장소 동기화 시 Then: 제외된 문서는 PlaceSyncWriter로 전달되지 않는다") {
                val result = service.sync()

                result.totalFetchedCount shouldBe 0
                result.rawCollectedCount shouldBe 8
                documentsSlot.captured shouldBe emptyList()
            }
        }
    }
})
