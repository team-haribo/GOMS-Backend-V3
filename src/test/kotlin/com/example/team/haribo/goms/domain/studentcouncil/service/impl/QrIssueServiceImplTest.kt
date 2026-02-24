package com.example.team.haribo.goms.domain.studentcouncil.service.impl

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.Instant

class QrIssueServiceImplTest : DescribeSpec({

    val service = QrIssueServiceImpl()

    describe("QrIssueService") {

        context("Given: QR 발급 요청") {
            it("When: issue() 호출 시 Then: UUID와 현재 시각+300초 만료 타임스탬프를 반환한다") {
                val response = service.issue()
                response.uuid shouldNotBe null
                response.uuid.length shouldBe 36
                response.exp shouldBeGreaterThan Instant.now().toEpochMilli()
                // exp는 현재 시각 + 약 5분(300,000ms) 이어야 함
                val expectedMin = Instant.now().plusSeconds(290).toEpochMilli()
                val expectedMax = Instant.now().plusSeconds(310).toEpochMilli()
                assert(response.exp in expectedMin..expectedMax) {
                    "exp(${response.exp}) is not in range [$expectedMin, $expectedMax]"
                }
            }
        }
    }
})
