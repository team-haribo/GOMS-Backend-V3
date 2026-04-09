package com.example.team.haribo.goms.domain.place.service.impl

import com.example.team.haribo.goms.domain.place.client.KakaoPlaceClient
import com.example.team.haribo.goms.domain.place.dto.response.KakaoPlaceDocument
import com.example.team.haribo.goms.domain.place.dto.response.PlaceSyncResult
import com.example.team.haribo.goms.domain.place.entity.Place
import com.example.team.haribo.goms.domain.common.enums.PlaceSyncCategory
import com.example.team.haribo.goms.domain.place.repository.PlaceRepository
import com.example.team.haribo.goms.domain.place.service.PlaceSyncService
import com.example.team.haribo.goms.domain.place.util.PlaceSyncFilter
import com.example.team.haribo.goms.global.log.LogFormat
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PlaceSyncServiceImpl(
    private val kakaoPlaceClient: KakaoPlaceClient,
    private val placeRepository: PlaceRepository,
    private val placeSyncFilter: PlaceSyncFilter
) : PlaceSyncService {

    private val log = LoggerFactory.getLogger(PlaceSyncServiceImpl::class.java)

    private val schoolLatitude = "35.1427689679488"
    private val schoolLongitude = "126.800771954215"
    private val radius = 1500
    private val pageSize = 15

    @Transactional
    override fun sync(): PlaceSyncResult {
        val syncStartedAt = LocalDateTime.now()
        val documents = fetchAllDocuments()

        var createdCount = 0
        var updatedCount = 0

        documents.forEach { document ->
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
                "createdCount" to createdCount,
                "updatedCount" to updatedCount,
                "deactivatedCount" to placesToDeactivate.size,
                "totalFetchedCount" to documents.size
            )
        )

        return PlaceSyncResult(
            createdCount = createdCount,
            updatedCount = updatedCount,
            deactivatedCount = placesToDeactivate.size,
            totalFetchedCount = documents.size
        )
    }

    private fun fetchAllDocuments(): List<KakaoPlaceDocument> {
        val result = linkedMapOf<String, KakaoPlaceDocument>()

        PlaceSyncCategory.entries.forEach { category ->
            var page = 1

            while (true) {
                val response = kakaoPlaceClient.searchByCategory(
                    categoryGroupCode = category.categoryGroupCode,
                    x = schoolLongitude,
                    y = schoolLatitude,
                    radius = radius,
                    page = page,
                    size = pageSize
                )

                response.documents
                    .filter { placeSyncFilter.isAllowed(it) }
                    .forEach { result[it.id] = it }

                if (response.meta.is_end) break
                page++
            }
        }

        return result.values.toList()
    }
}