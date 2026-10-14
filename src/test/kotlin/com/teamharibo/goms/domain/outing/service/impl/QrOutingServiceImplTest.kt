package com.teamharibo.goms.domain.outing.service.impl

import com.teamharibo.goms.domain.common.enums.Action
import com.teamharibo.goms.domain.common.enums.Status
import com.teamharibo.goms.domain.outing.dto.request.QrToggleRequest
import com.teamharibo.goms.domain.outing.exception.AlreadyOutingException
import com.teamharibo.goms.domain.outing.exception.CannotOutingException
import com.teamharibo.goms.domain.outing.exception.QrExpiredException
import com.teamharibo.goms.domain.outing.repository.OutingRepository
import com.teamharibo.goms.fixture.MemberFixture
import com.teamharibo.goms.fixture.OutingFixture
import com.teamharibo.goms.global.util.MemberUtil
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class QrOutingServiceImplTest : DescribeSpec({

    val memberUtil = mockk<MemberUtil>()
    val outingRepository = mockk<OutingRepository>()
    val service = QrOutingServiceImpl(memberUtil, outingRepository)

    describe("QrOutingService") {

        context("Given: COMING 상태 멤버 + 유효한 QR") {
            val member = MemberFixture.student(status = Status.COMING)
            every { memberUtil.currentMemberForUpdate() } returns member
            every { outingRepository.save(any()) } answers {
                firstArg<com.teamharibo.goms.domain.outing.entity.Outing>().also { it.id = 10L }
            }

            it("When: 외출 QR 스캔 시 Then: 상태가 OUTING으로 변경되고 Outing 레코드가 저장된다") {
                val response = service.outing(QrToggleRequest(uuid = "uuid-1", exp = OutingFixture.futureExpMs()))
                response.action shouldBe Action.OUT
                response.status shouldBe Status.OUTING
                member.status shouldBe Status.OUTING
                verify(exactly = 1) { outingRepository.save(any()) }
            }
        }

        context("Given: CANNOT_OUTING 상태 멤버") {
            every { memberUtil.currentMemberForUpdate() } returns MemberFixture.cannotOuting()

            it("When: 외출 QR 스캔 시 Then: CannotOutingException이 발생한다") {
                shouldThrow<CannotOutingException> {
                    service.outing(QrToggleRequest(uuid = "uuid-1", exp = OutingFixture.futureExpMs()))
                }
            }
        }

        context("Given: 이미 OUTING 상태 멤버") {
            every { memberUtil.currentMemberForUpdate() } returns MemberFixture.outing()

            it("When: 외출 QR 스캔 시 Then: AlreadyOutingException이 발생한다") {
                shouldThrow<AlreadyOutingException> {
                    service.outing(QrToggleRequest(uuid = "uuid-1", exp = OutingFixture.futureExpMs()))
                }
            }
        }

        context("Given: 만료된 QR exp") {
            it("When: 외출 QR 스캔 시 Then: QrExpiredException이 발생한다") {
                shouldThrow<QrExpiredException> {
                    service.outing(QrToggleRequest(uuid = "uuid-1", exp = OutingFixture.pastExpMs()))
                }
            }
        }
    }
})
