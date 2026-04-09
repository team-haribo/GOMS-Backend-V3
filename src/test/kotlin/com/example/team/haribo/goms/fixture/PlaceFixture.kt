package com.example.team.haribo.goms.fixture

import com.example.team.haribo.goms.domain.member.entity.Member
import com.example.team.haribo.goms.domain.place.entity.Place
import com.example.team.haribo.goms.domain.place.entity.PlaceRecommend
import java.time.LocalDateTime

object PlaceFixture {

    fun place(
        id: Long = 1L,
        externalPlaceId: String = "test-place-id-$id",
        placeName: String = "테스트 장소",
        address: String = "서울시 중구 세종대로 110",
        roadAddress: String? = "서울특별시 중구 세종대로 110",
        latitude: Double = 37.5665,
        longitude: Double = 126.9780,
        categoryGroupCode: String? = "FD6",
        categoryGroupName: String? = "음식점",
        categoryName: String? = "한식",
        phone: String? = "010-0000-0000",
        placeUrl: String? = "https://place.map.kakao.com/123456",
        isActive: Boolean = true,
        lastSyncedAt: LocalDateTime = LocalDateTime.now(),
        createdAt: LocalDateTime = LocalDateTime.now(),
        updatedAt: LocalDateTime = LocalDateTime.now()
    ) = Place(
        id = id,
        externalPlaceId = externalPlaceId,
        placeName = placeName,
        address = address,
        roadAddress = roadAddress,
        latitude = latitude,
        longitude = longitude,
        categoryGroupCode = categoryGroupCode,
        categoryGroupName = categoryGroupName,
        categoryName = categoryName,
        phone = phone,
        placeUrl = placeUrl,
        isActive = isActive,
        lastSyncedAt = lastSyncedAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun recommend(
        id: Long? = 1L,
        place: Place = place(),
        member: Member = MemberFixture.student(),
        recommended: Boolean = true
    ) = PlaceRecommend(
        id = id,
        place = place,
        member = member,
        recommended = recommended,
        createdAt = LocalDateTime.now()
    )
}