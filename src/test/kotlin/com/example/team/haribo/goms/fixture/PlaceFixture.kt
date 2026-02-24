package com.example.team.haribo.goms.fixture

import com.example.team.haribo.goms.domain.member.entity.Member
import com.example.team.haribo.goms.domain.place.entity.Place
import com.example.team.haribo.goms.domain.place.entity.PlaceRecommend
import java.time.LocalDateTime

object PlaceFixture {

    fun place(id: Long = 1L) = Place(
        id = id,
        latitude = 37.5665,
        longitude = 126.9780,
        placeName = "테스트 장소",
        address = "서울시 중구 세종대로 110"
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
