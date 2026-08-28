package com.example.team.haribo.goms.domain.place.service

import com.example.team.haribo.goms.domain.place.dto.response.KakaoPlaceDocument
import com.example.team.haribo.goms.domain.place.entity.Place
import com.example.team.haribo.goms.domain.place.repository.PlaceRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * PlaceSyncServiceImpl.sync()에서 카카오 API 호출(외부 I/O) 단계와 DB 반영 단계를 분리하기 위한 컴포넌트.
 * 최대 수백~천 회에 달하는 외부 API 호출은 트랜잭션 밖에서 끝내고, DB 반영만 이 클래스의
 * 짧은 트랜잭션 안에서 처리해 커넥션 점유 시간을 줄인다.
 */
@Component
class PlaceSyncWriter(
    private val placeRepository: PlaceRepository
) {

    @Transactional
    fun write(documents: List<KakaoPlaceDocument>, syncStartedAt: LocalDateTime): PlaceSyncWriteResult {
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

        return PlaceSyncWriteResult(
            createdCount = createdCount,
            updatedCount = updatedCount,
            deactivatedCount = placesToDeactivate.size
        )
    }
}

data class PlaceSyncWriteResult(
    val createdCount: Int,
    val updatedCount: Int,
    val deactivatedCount: Int
)
