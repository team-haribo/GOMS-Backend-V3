package com.example.team.haribo.goms.domain.place.service.impl

import com.example.team.haribo.goms.domain.common.enums.PlaceSyncCategory
import com.example.team.haribo.goms.domain.place.client.KakaoPlaceClient
import com.example.team.haribo.goms.domain.place.dto.response.KakaoPlaceDocument
import com.example.team.haribo.goms.domain.place.dto.response.PlaceSyncResult
import com.example.team.haribo.goms.domain.place.service.PlaceSyncService
import com.example.team.haribo.goms.domain.place.service.PlaceSyncWriter
import com.example.team.haribo.goms.domain.place.util.PlaceDistanceCalculator
import com.example.team.haribo.goms.domain.place.util.PlaceSearchPointGenerator
import com.example.team.haribo.goms.domain.place.util.PlaceSyncFilter
import com.example.team.haribo.goms.domain.place.util.SearchPoint
import com.example.team.haribo.goms.global.log.LogFormat
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class PlaceSyncServiceImpl(
    private val kakaoPlaceClient: KakaoPlaceClient,
    private val placeSyncWriter: PlaceSyncWriter,
    private val placeSyncFilter: PlaceSyncFilter,
    private val placeSearchPointGenerator: PlaceSearchPointGenerator,
    private val placeDistanceCalculator: PlaceDistanceCalculator
) : PlaceSyncService {

    private val log = LoggerFactory.getLogger(PlaceSyncServiceImpl::class.java)

    private val schoolLatitude = 35.1427689679488
    private val schoolLongitude = 126.800771954215
    private val finalRadius = 1000
    private val searchRadius = 350
    private val searchPointOffsetMeter = 250.0
    private val pageSize = 15
    private val maxPage = 3

    /**
     * 카카오 API 호출(최대 수백~천 회에 달하는 외부 I/O)과 DB 반영을 한 트랜잭션 안에서 처리하면
     * 외부 API 응답을 기다리는 동안 DB 커넥션을 계속 점유하게 된다.
     * 그래서 이 메서드 자체는 트랜잭션을 걸지 않고, 외부 호출(fetchAllDocuments)을 먼저 끝낸 뒤
     * DB 반영만 PlaceSyncWriter의 짧은 트랜잭션 안에서 처리하도록 분리했다.
     */
    override fun sync(): PlaceSyncResult {
        val syncStartedAt = LocalDateTime.now()
        val searchPoints = placeSearchPointGenerator.generate(
            centerLatitude = schoolLatitude,
            centerLongitude = schoolLongitude,
            offsetMeter = searchPointOffsetMeter,
            searchRadius = searchRadius
        )

        val fetchResult = fetchAllDocuments(searchPoints)

        val writeResult = placeSyncWriter.write(fetchResult.documents, syncStartedAt)

        log.info(
            LogFormat.message(
                domain = "PLACE",
                event = "장소 동기화 완료",
                "searchPointCount" to searchPoints.size,
                "rawCollectedCount" to fetchResult.rawCollectedCount,
                "totalFetchedCount" to fetchResult.documents.size,
                "createdCount" to writeResult.createdCount,
                "updatedCount" to writeResult.updatedCount,
                "deactivatedCount" to writeResult.deactivatedCount
            )
        )

        return PlaceSyncResult(
            createdCount = writeResult.createdCount,
            updatedCount = writeResult.updatedCount,
            deactivatedCount = writeResult.deactivatedCount,
            totalFetchedCount = fetchResult.documents.size,
            searchPointCount = searchPoints.size,
            rawCollectedCount = fetchResult.rawCollectedCount
        )
    }

    private fun fetchAllDocuments(searchPoints: List<SearchPoint>): FetchResult {
        val result = linkedMapOf<String, KakaoPlaceDocument>()
        var rawCollectedCount = 0

        PlaceSyncCategory.entries.forEach { category ->
            searchPoints.forEach { point ->
                var page = 1

                while (page <= maxPage) {
                    val response = kakaoPlaceClient.searchByCategory(
                        categoryGroupCode = category.categoryGroupCode,
                        x = point.longitude.toString(),
                        y = point.latitude.toString(),
                        radius = point.radius,
                        page = page,
                        size = pageSize
                    )

                    rawCollectedCount += response.documents.size

                    response.documents
                        .filter { placeSyncFilter.isAllowed(it) }
                        .filter { !result.containsKey(it.id) }
                        .filter { isWithinSchoolRadius(it) }
                        .forEach { result[it.id] = it }

                    if (response.meta.is_end) break
                    page++
                }
            }
        }

        return FetchResult(
            documents = result.values.toList(),
            rawCollectedCount = rawCollectedCount
        )
    }

    private fun isWithinSchoolRadius(document: KakaoPlaceDocument): Boolean {
        return placeDistanceCalculator.isWithinRadius(
            originLatitude = schoolLatitude,
            originLongitude = schoolLongitude,
            targetLatitude = document.y.toDouble(),
            targetLongitude = document.x.toDouble(),
            radiusMeter = finalRadius
        )
    }

    private data class FetchResult(
        val documents: List<KakaoPlaceDocument>,
        val rawCollectedCount: Int
    )
}
