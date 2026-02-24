package com.example.team.haribo.goms.domain.outing.service.impl

import com.example.team.haribo.goms.domain.outing.repository.OutingRepository
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class OutingCountServiceImplTest : DescribeSpec({

    val outingRepository = mockk<OutingRepository>()
    val service = OutingCountServiceImpl(outingRepository)

    describe("OutingCountService") {

        context("Given: 활성 외출자 3명") {
            every { outingRepository.countActive() } returns 3L

            it("When: 외출 인원 수 조회 시 Then: 3을 반환한다") {
                val response = service.getCount()
                response.outingCount shouldBe 3L
            }
        }

        context("Given: 외출자 없음") {
            every { outingRepository.countActive() } returns 0L

            it("When: 외출 인원 수 조회 시 Then: 0을 반환한다") {
                val response = service.getCount()
                response.outingCount shouldBe 0L
            }
        }
    }
})
