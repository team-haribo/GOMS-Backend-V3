package com.example.team.haribo.goms.fixture

import com.example.team.haribo.goms.domain.member.entity.Member
import com.example.team.haribo.goms.domain.place.entity.Place
import com.example.team.haribo.goms.domain.review.entity.Review
import java.time.LocalDateTime

object ReviewFixture {

    fun review(
        id: Long = 1L,
        place: Place = PlaceFixture.place(),
        member: Member = MemberFixture.student(),
        content: String = "좋은 장소입니다."
    ) = Review(
        id = id,
        place = place,
        member = member,
        content = content
    ).also { it.createdAt = LocalDateTime.now() }

    fun longContent(length: Int = 501) = "a".repeat(length)
}