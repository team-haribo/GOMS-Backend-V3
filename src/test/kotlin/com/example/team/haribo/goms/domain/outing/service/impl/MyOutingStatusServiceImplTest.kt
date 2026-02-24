package com.example.team.haribo.goms.domain.outing.service.impl

import com.example.team.haribo.goms.domain.common.enums.Status
import com.example.team.haribo.goms.fixture.MemberFixture
import com.example.team.haribo.goms.global.util.MemberUtil
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class MyOutingStatusServiceImplTest : DescribeSpec({

    val memberUtil = mockk<MemberUtil>()
    val service = MyOutingStatusServiceImpl(memberUtil)

    describe("MyOutingStatusService") {

        context("Given: COMING 상태 멤버") {
            val member = MemberFixture.student(status = Status.COMING)
            every { memberUtil.currentMember() } returns member

            it("When: 외출 상태 조회 시 Then: COMING 상태를 반환한다") {
                val response = service.getStatus()
                response.status shouldBe Status.COMING
                response.memberId shouldBe member.id!!
                response.name shouldBe member.name
            }
        }

        context("Given: OUTING 상태 멤버") {
            val member = MemberFixture.outing()
            every { memberUtil.currentMember() } returns member

            it("When: 외출 상태 조회 시 Then: OUTING 상태를 반환한다") {
                val response = service.getStatus()
                response.status shouldBe Status.OUTING
            }
        }
    }
})
