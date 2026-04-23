package com.example.team.haribo.goms.domain.studentcouncil.service.impl

import com.example.team.haribo.goms.domain.late.repository.LateRepository
import com.example.team.haribo.goms.fixture.LateFixture
import com.example.team.haribo.goms.fixture.MemberFixture
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.time.LocalDate
import java.time.LocalDateTime

class StudentCouncilLateListServiceImplTest : DescribeSpec({

    val lateRepository = mockk<LateRepository>()
    val service = StudentCouncilLateListServiceImpl(lateRepository)

    describe("StudentCouncilLateListService") {

        context("Given: 특정 date 지정") {
            val targetDate = LocalDate.of(2026, 2, 24)
            val expectedStart = targetDate.atStartOfDay()
            val expectedEnd = targetDate.plusDays(1).atStartOfDay()
            val member = MemberFixture.student(id = 1L)
            val lates = listOf(LateFixture.late(member = member))

            val startSlot = slot<LocalDateTime>()
            val endSlot = slot<LocalDateTime>()
            every {
                lateRepository.findAllByComingAtRangeWithMember(capture(startSlot), capture(endSlot))
            } returns lates

            it("When: 목록 조회 시 Then: 해당 날짜 범위로 쿼리하고 결과를 반환한다") {
                val response = service.list(targetDate)
                response.students.size shouldBe 1
                response.students[0].memberId shouldBe member.id!!
                response.students[0].role shouldBe member.role
                response.students[0].status shouldBe member.status
                startSlot.captured shouldBe expectedStart
                endSlot.captured shouldBe expectedEnd
            }
        }

        context("Given: date=null (오늘 기본값)") {
            val today = LocalDate.now()
            val expectedStart = today.atStartOfDay()
            val expectedEnd = today.plusDays(1).atStartOfDay()

            val startSlot = slot<LocalDateTime>()
            val endSlot = slot<LocalDateTime>()
            every {
                lateRepository.findAllByComingAtRangeWithMember(capture(startSlot), capture(endSlot))
            } returns emptyList()

            it("When: 목록 조회 시 Then: 오늘 날짜를 기본값으로 쿼리한다") {
                service.list(null)
                startSlot.captured shouldBe expectedStart
                endSlot.captured shouldBe expectedEnd
            }
        }

        context("Given: 결과 없음") {
            every { lateRepository.findAllByComingAtRangeWithMember(any(), any()) } returns emptyList()

            it("When: 목록 조회 시 Then: 빈 리스트를 반환한다") {
                val response = service.list(LocalDate.now())
                response.students.shouldBeEmpty()
            }
        }
    }
})