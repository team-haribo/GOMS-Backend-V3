package com.example.team.haribo.goms.domain.place.service.impl

import com.example.team.haribo.goms.domain.common.enums.PlaceSyncCategory
import com.example.team.haribo.goms.domain.place.client.KakaoPlaceClient
import com.example.team.haribo.goms.domain.place.dto.response.KakaoPlaceDocument
import com.example.team.haribo.goms.domain.place.dto.response.PlaceSyncResult
import com.example.team.haribo.goms.domain.place.entity.Place
import com.example.team.haribo.goms.domain.place.repository.PlaceRepository
import com.example.team.haribo.goms.domain.place.service.PlaceSyncService
import com.example.team.haribo.goms.domain.place.util.PlaceDistanceCalculator
import com.example.team.haribo.goms.domain.place.util.PlaceSearchPointGenerator
import com.example.team.haribo.goms.domain.place.util.PlaceSyncFilter
import com.example.team.haribo.goms.domain.place.util.SearchPoint
import com.example.team.haribo.goms.global.log.LogFormat
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PlaceSyncServiceImpl(
    private val kakaoPlaceClient: KakaoPlaceClient,
    private val placeRepository: PlaceRepository,
    private val placeSyncFilter: PlaceSyncFilter,
    private val placeSearchPointGenerator: PlaceSearchPointGenerator,
    private val placeDistanceCalculator: PlaceDistanceCalculator
) : PlaceSyncService {

    private val log = LoggerFactory.getLogger(PlaceSyncServiceImpl::class.java)

    private val schoolLatitude = 35.1427689679488
    private val schoolLongitude = 126.800771954215
    private val finalRadius = 1000
    private val searchRadius = 500
    private val searchPointOffsetMeter = 500.0
    private val pageSize = 15
    private val maxPage = 3

    @Transactional
    override fun sync(): PlaceSyncResult {
        val syncStartedAt = LocalDateTime.now()
        val searchPoints = placeSearchPointGenerator.generate(
            centerLatitude = schoolLatitude,
            centerLongitude = schoolLongitude,
            offsetMeter = searchPointOffsetMeter,
            searchRadius = searchRadius
        )

        val fetchResult = fetchAllDocuments(searchPoints)

        var createdCount = 0
        var updatedCount = 0

        fetchResult.documents.forEach { document ->
            val existing = placeRepository.findByExternalPlaceId(document.id).orElse(null)

            if (existing == null) {
                placeRepository.save(
                    Place(
                        externalPlaceId = document.id,
                        placeName = document.place_name,
                        address = document.address_name,
                        roadAddress = document.road_address_name,
                        latitude = document.y.toDouble(),
                        longitude = document.x.toDouble(),
                        categoryGroupCode = document.category_group_code,
                        categoryGroupName = document.category_group_name,
                        categoryName = document.category_name,
                        phone = document.phone,
                        placeUrl = document.place_url,
                        isActive = true,
                        lastSyncedAt = syncStartedAt,
                        createdAt = syncStartedAt,
                        updatedAt = syncStartedAt
                    )
                )
                createdCount++
            } else {
                existing.sync(
                    placeName = document.place_name,
                    address = document.address_name,
                    roadAddress = document.road_address_name,
                    latitude = document.y.toDouble(),
                    longitude = document.x.toDouble(),
                    categoryGroupCode = document.category_group_code,
                    categoryGroupName = document.category_group_name,
                    categoryName = document.category_name,
                    phone = document.phone,
                    placeUrl = document.place_url,
                    syncedAt = syncStartedAt
                )
                updatedCount++
            }
        }

        val placesToDeactivate = placeRepository.findAllByLastSyncedAtBeforeAndIsActiveTrue(syncStartedAt)
        placesToDeactivate.forEach { it.deactivate(syncStartedAt) }

        log.info(
            LogFormat.message(
                domain = "PLACE",
                event = "장소 동기화 완료",
                "searchPointCount" to searchPoints.size,
                "rawCollectedCount" to fetchResult.rawCollectedCount,
                "totalFetchedCount" to fetchResult.documents.size,
                "createdCount" to createdCount,
                "updatedCount" to updatedCount,
                "deactivatedCount" to placesToDeactivate.size
            )
        )

        return PlaceSyncResult(
            createdCount = createdCount,
            updatedCount = updatedCount,
            deactivatedCount = placesToDeactivate.size,
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