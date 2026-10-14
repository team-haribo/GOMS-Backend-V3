package com.teamharibo.goms.fixture

import com.teamharibo.goms.domain.member.entity.Member
import com.teamharibo.goms.domain.outing.entity.Outing
import java.time.LocalDateTime

object OutingFixture {

    fun active(member: Member = MemberFixture.student()) = Outing(
        member = member,
        outingAt = LocalDateTime.now().minusMinutes(30)
    ).also { it.id = 1L }

    fun completed(member: Member = MemberFixture.student()) = Outing(
        member = member,
        outingAt = LocalDateTime.now().minusHours(2),
        comingAt = LocalDateTime.now().minusHours(1)
    ).also { it.id = 2L }

    /** exp 값: 현재 시각 + 1시간 (밀리초), 항상 유효 */
    fun futureExpMs(): Long = System.currentTimeMillis() + 3_600_000L

    /** exp 값: 과거 (밀리초), 항상 만료 */
    fun pastExpMs(): Long = 1_000L

    /** exp 값: 현재 시각 + 1시간 (초), 항상 유효 */
    fun futureExpSec(): Long = (System.currentTimeMillis() / 1000) + 3_600L

    /** exp 값: 과거 (초), 항상 만료 */
    fun pastExpSec(): Long = 1L
}
