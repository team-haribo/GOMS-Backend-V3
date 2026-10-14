package com.teamharibo.goms.fixture

import com.teamharibo.goms.domain.late.entity.Late
import com.teamharibo.goms.domain.member.entity.Member
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
