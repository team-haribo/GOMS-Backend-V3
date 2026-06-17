package com.example.team.haribo.goms.domain.late.job

import com.example.team.haribo.goms.domain.common.enums.Status
import com.example.team.haribo.goms.domain.late.entity.Late
import com.example.team.haribo.goms.domain.late.repository.MemberLateCount
import com.example.team.haribo.goms.domain.late.repository.LateRepository
import com.example.team.haribo.goms.domain.outing.repository.OutingRepository
import com.example.team.haribo.goms.fixture.MemberFixture
import com.example.team.haribo.goms.fixture.OutingFixture
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify

class LateAutoCreateJobTest : DescribeSpec({

    describe("LateAutoCreateJob") {

        context("Given: OUTING 상태 활성 외출자가 존재한다") {
            val member = MemberFixture.outing(id = 1L)
            val outing = OutingFixture.active(member)
            val lateSlot = slot<Iterable<Late>>()
            val outingRepository = mockk<OutingRepository>()
            val lateRepository = mockk<LateRepository>()
            val job = LateAutoCreateJob(outingRepository, lateRepository)
            val memberLateCount = mockk<MemberLateCount>()

            every { outingRepository.findAllActiveWithOutingMember() } returns listOf(outing)
            every { lateRepository.findAllOutingIdsIn(listOf(outing.id!!)) } returns emptyList()
            every { lateRepository.countByMemberIds(listOf(member.id!!)) } returns listOf(memberLateCount)
            every { memberLateCount.memberId } returns member.id!!
            every { memberLateCount.lateCount } returns 2L
            every { lateRepository.saveAll(capture(lateSlot)) } answers { firstArg<Iterable<Late>>().toList() }

            it("When: 자동 지각 생성 시 Then: Late를 저장하고 외출 상태를 COMING으로 변경한다") {
                job.createLatesForOutingMembers()

                val late = lateSlot.captured.single()
                late.member shouldBe member
                late.outing shouldBe outing
                late.lateCount shouldBe 3L
                late.comingAt shouldNotBe null
                outing.comingAt shouldNotBe null
                member.status shouldBe Status.COMING
            }
        }

        context("Given: 이미 같은 외출 건으로 지각이 생성되었다") {
            val member = MemberFixture.outing(id = 2L)
            val outing = OutingFixture.active(member)
            val outingRepository = mockk<OutingRepository>()
            val lateRepository = mockk<LateRepository>()
            val job = LateAutoCreateJob(outingRepository, lateRepository)

            every { outingRepository.findAllActiveWithOutingMember() } returns listOf(outing)
            every { lateRepository.findAllOutingIdsIn(listOf(outing.id!!)) } returns listOf(outing.id!!)
            every { lateRepository.saveAll(emptyList<Late>()) } returns emptyList()

            it("When: 자동 지각 생성 시 Then: 중복 저장하지 않고 상태를 변경하지 않는다") {
                job.createLatesForOutingMembers()

                member.status shouldBe Status.OUTING
                outing.comingAt shouldBe null
                verify(exactly = 0) { lateRepository.countByMemberId(any()) }
                verify(exactly = 0) { lateRepository.countByMemberIds(any()) }
            }
        }
    }
})
