package com.example.team.haribo.goms.domain.notification.service.impl

import com.example.team.haribo.goms.domain.common.enums.Status
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.fixture.MemberFixture
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class OutingMemberQueryServiceImplTest : DescribeSpec({

    val memberRepository = mockk<MemberRepository>()
    val service = OutingMemberQueryServiceImpl(memberRepository)

    describe("OutingMemberQueryService") {

        context("Given: 외출 중인 학생이 있음") {
            val members = listOf(
                MemberFixture.outing(id = 1L),
                MemberFixture.outing(id = 2L)
            )

            every { memberRepository.findAllByStatus(Status.OUTING) } returns members

            it("When: 외출 중인 학생 ID 조회 시 Then: memberId 리스트를 반환한다") {
                val result = service.getOutingMemberIds()
                result shouldBe listOf(1L, 2L)
            }
        }

        context("Given: 외출 중인 학생이 없음") {
            every { memberRepository.findAllByStatus(Status.OUTING) } returns emptyList()

            it("When: 외출 중인 학생 ID 조회 시 Then: 빈 리스트를 반환한다") {
                val result = service.getOutingMemberIds()
                result shouldBe emptyList()
            }
        }
    }
})