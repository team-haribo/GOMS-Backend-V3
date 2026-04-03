package com.example.team.haribo.goms.domain.outing.service.impl

import com.example.team.haribo.goms.domain.common.enums.Status
import com.example.team.haribo.goms.domain.late.repository.LateRepository
import com.example.team.haribo.goms.fixture.MemberFixture
import com.example.team.haribo.goms.global.util.MemberUtil
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class MyOutingStatusServiceImplTest : DescribeSpec({

    val memberUtil = mockk<MemberUtil>()
    val lateRepository = mockk<LateRepository>()
    val service = MyOutingStatusServiceImpl(memberUtil, lateRepository)

    describe("MyOutingStatusService") {

        context("Given: COMING 상태 멤버") {
            val member = MemberFixture.student(status = Status.COMING)
            every { memberUtil.currentMember() } returns member
            every { lateRepository.countByMemberId(member.id!!) } returns 0L

            it("When: 외출 상태 조회 시 Then: COMING 상태를 반환한다") {
                val response = service.getStatus()
                response.status shouldBe Status.COMING
                response.memberId shouldBe member.id!!
                response.name shouldBe member.name
                response.grade shouldBe member.grade
                response.department shouldBe member.department.name
                response.lateCount shouldBe 0L
            }
        }

        context("Given: OUTING 상태 멤버") {
            val member = MemberFixture.outing()
            every { memberUtil.currentMember() } returns member
            every { lateRepository.countByMemberId(member.id!!) } returns 2L

            it("When: 외출 상태 조회 시 Then: OUTING 상태를 반환한다") {
                val response = service.getStatus()
                response.status shouldBe Status.OUTING
                response.memberId shouldBe member.id!!
                response.name shouldBe member.name
                response.grade shouldBe member.grade
                response.department shouldBe member.department.name
                response.lateCount shouldBe 2L
            }
        }
    }
})