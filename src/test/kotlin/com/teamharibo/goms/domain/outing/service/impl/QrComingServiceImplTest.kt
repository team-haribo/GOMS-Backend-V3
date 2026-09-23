package com.teamharibo.goms.domain.outing.service.impl

import com.teamharibo.goms.domain.common.enums.Action
import com.teamharibo.goms.domain.common.enums.Status
import com.teamharibo.goms.domain.outing.dto.request.QrToggleRequest
import com.teamharibo.goms.domain.outing.exception.NotOutingException
import com.teamharibo.goms.domain.outing.exception.QrExpiredException
import com.teamharibo.goms.domain.outing.repository.OutingRepository
import com.teamharibo.goms.fixture.MemberFixture
import com.teamharibo.goms.fixture.OutingFixture
import com.teamharibo.goms.global.util.MemberUtil
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk

class QrComingServiceImplTest : DescribeSpec({

    val memberUtil = mockk<MemberUtil>()
    val outingRepository = mockk<OutingRepository>()
    val service = QrComingServiceImpl(memberUtil, outingRepository)

    describe("QrComingService") {

        context("Given: OUTING 상태 멤버 + 유효 QR + 활성 Outing 존재") {
            val member = MemberFixture.outing()
            val activeOuting = OutingFixture.active(member)
            every { memberUtil.currentMemberForUpdate() } returns member
            every { outingRepository.findTopByMemberIdAndComingAtIsNullOrderByIdDesc(member.id!!) } returns activeOuting

            it("When: 귀교 QR 스캔 시 Then: comingAt이 설정되고 상태가 COMING으로 변경된다") {
                val response = service.coming(QrToggleRequest(uuid = "uuid-1", exp = OutingFixture.futureExpMs()))
                response.action shouldBe Action.IN
                response.status shouldBe Status.COMING
                response.outingId shouldBe activeOuting.id!!
                activeOuting.comingAt shouldNotBe null
                member.status shouldBe Status.COMING
            }
        }

        context("Given: 활성 Outing 없음") {
            val member = MemberFixture.student()
            every { memberUtil.currentMemberForUpdate() } returns member
            every { outingRepository.findTopByMemberIdAndComingAtIsNullOrderByIdDesc(any()) } returns null

            it("When: 귀교 QR 스캔 시 Then: NotOutingException이 발생한다") {
                shouldThrow<NotOutingException> {
                    service.coming(QrToggleRequest(uuid = "uuid-1", exp = OutingFixture.futureExpMs()))
                }
            }
        }

        context("Given: 만료된 QR exp") {
            it("When: 귀교 QR 스캔 시 Then: QrExpiredException이 발생한다") {
                shouldThrow<QrExpiredException> {
                    service.coming(QrToggleRequest(uuid = "uuid-1", exp = OutingFixture.pastExpMs()))
                }
            }
        }
    }
})
