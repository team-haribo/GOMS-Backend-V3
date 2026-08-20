package com.example.team.haribo.goms.domain.place.service.impl

import com.example.team.haribo.goms.domain.place.client.KakaoPlaceClient
import com.example.team.haribo.goms.domain.place.dto.response.KakaoPlaceMeta
import com.example.team.haribo.goms.domain.place.dto.response.KakaoPlaceSearchResponse
import com.example.team.haribo.goms.domain.place.entity.Place
import com.example.team.haribo.goms.domain.place.repository.PlaceRepository
import com.example.team.haribo.goms.domain.place.service.PlaceSyncWriter
import com.example.team.haribo.goms.domain.place.util.PlaceDistanceCalculator
import com.example.team.haribo.goms.domain.place.util.PlaceSearchPointGenerator
import com.example.team.haribo.goms.domain.place.util.PlaceSyncFilter
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.time.LocalDateTime

/**
 * PlaceSyncServiceImpl.sync()가 카카오 API 호출(외부 I/O)을 트랜잭션 밖에서 수행하고,
 * DB 반영(PlaceSyncWriter)만 짧은 트랜잭션 안에서 커밋하는지 검증한다.
 *
 * @DataJpaTest 기본 롤백 트랜잭션을 쓰면 sync() 내부에서 커밋되는 PlaceSyncWriter의
 * 트랜잭션을 관찰할 수 없으므로, 클래스 레벨에 Propagation.NOT_SUPPORTED를 걸어
 * 테스트 메서드 자체는 트랜잭션 밖에서 실행되게 한다.
 */
@DataJpaTest
@TestPropertySource(properties = ["spring.profiles.active="])
@Import(PlaceSyncWriter::class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PlaceSyncTransactionScopeTest @Autowired constructor(
    private val placeRepository: PlaceRepository,
    private val placeSyncWriter: PlaceSyncWriter
) {

    @Test
    fun `외부 API 호출 중에는 트랜잭션이 열려있지 않고, DB 반영은 정상적으로 커밋된다`() {
        val stalePlace = placeRepository.save(
            Place(
                externalPlaceId = "stale-place",
                placeName = "정리 대상 장소",
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
        )

        var callCount = 0
        val transactionActiveDuringFetch = mutableListOf<Boolean>()
        val kakaoPlaceClient = mockk<KakaoPlaceClient>()
        every {
            kakaoPlaceClient.searchByCategory(
                categoryGroupCode = any(),
                x = any(),
                y = any(),
                radius = any(),
                page = any(),
                size = any()
            )
        } answers {
            callCount++
            transactionActiveDuringFetch.add(TransactionSynchronizationManager.isActualTransactionActive())
            KakaoPlaceSearchResponse(
                meta = KakaoPlaceMeta(total_count = 0, pageable_count = 0, is_end = true),
                documents = emptyList()
            )
        }

        val service = PlaceSyncServiceImpl(
            kakaoPlaceClient = kakaoPlaceClient,
            placeSyncWriter = placeSyncWriter,
            placeSyncFilter = PlaceSyncFilter(),
            placeSearchPointGenerator = PlaceSearchPointGenerator(),
            placeDistanceCalculator = PlaceDistanceCalculator()
        )

        service.sync()

        // 검색 좌표 7x7=49개 x 카테고리 8개 = 392회 호출되어야 한다 (is_end=true 이므로 페이지당 1회)
        assertEquals(392, callCount, "외부 API 호출 횟수가 예상과 다르다")
        assertTrue(
            transactionActiveDuringFetch.all { !it },
            "외부 API 호출 중에는 활성 트랜잭션이 없어야 한다 (트랜잭션이 커넥션을 점유한 채 대기하면 안 됨)"
        )

        val reloaded = placeRepository.findById(requireNotNull(stalePlace.id)).orElseThrow()
        assertFalse(reloaded.isActive, "동기화 대상에서 빠진 기존 장소는 PlaceSyncWriter의 트랜잭션 안에서 비활성화·커밋되어야 한다")
    }
}
