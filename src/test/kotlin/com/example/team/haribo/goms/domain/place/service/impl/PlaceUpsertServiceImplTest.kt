package com.example.team.haribo.goms.domain.place.service.impl

import com.example.team.haribo.goms.domain.place.dto.request.PlaceUpsertRequest
import com.example.team.haribo.goms.domain.place.repository.PlaceRepository
import com.example.team.haribo.goms.fixture.PlaceFixture
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.Optional

class PlaceUpsertServiceImplTest : DescribeSpec({

    val placeRepository = mockk<PlaceRepository>()
    val service = PlaceUpsertServiceImpl(placeRepository)

    val request = PlaceUpsertRequest(
        placeName = "새 장소",
        address = "서울시 강남구 테헤란로 1",
        latitude = 37.5665,
        longitude = 126.9780
    )

    describe("PlaceUpsertService") {

        context("Given: 새 좌표 (DB에 없는 위치)") {
            every { placeRepository.findByLatitudeAndLongitude(any(), any()) } returns Optional.empty()
            every { placeRepository.save(any()) } answers {
                firstArg<com.example.team.haribo.goms.domain.place.entity.Place>().also {
                    // id는 val이므로 생성자에서 설정 불가, save 반환값 별도 생성
                }
                PlaceFixture.place(id = 99L)
            }

            it("When: upsert 시 Then: 신규 Place를 저장하고 ID를 반환한다") {
                val response = service.upsert(request)
                response.placeId shouldBe 99L
                verify(exactly = 1) { placeRepository.save(any()) }
            }
        }

        context("Given: 기존 좌표 (DB에 이미 존재)") {
            val existing = PlaceFixture.place(id = 5L)
            every { placeRepository.findByLatitudeAndLongitude(any(), any()) } returns Optional.of(existing)
            every { placeRepository.save(existing) } returns existing

            it("When: upsert 시 Then: 기존 Place의 name/address를 업데이트한다") {
                val response = service.upsert(request.copy(placeName = "수정된 장소"))
                response.placeId shouldBe 5L
                existing.placeName shouldBe "수정된 장소"
                existing.address shouldBe request.address
                verify(exactly = 1) { placeRepository.save(existing) }
            }
        }
    }
})
