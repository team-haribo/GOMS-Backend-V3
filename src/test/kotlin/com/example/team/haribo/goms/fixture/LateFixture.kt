package com.example.team.haribo.goms.fixture

import com.example.team.haribo.goms.domain.late.entity.Late
import com.example.team.haribo.goms.domain.member.entity.Member
import java.time.LocalDateTime

object LateFixture {

    fun late(
        member: Member = MemberFixture.student(),
        lateCount: Long = 5L,
        comingAt: LocalDateTime = LocalDateTime.now().minusHours(1)
    ) = Late(
        member = member,
        outing = OutingFixture.active(member),
        comingAt = comingAt,
        lateCount = lateCount
    ).also { it.id = 1L }
}
