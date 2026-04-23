package com.example.team.haribo.goms.domain.outing.util

import com.example.team.haribo.goms.domain.outing.exception.QrExpiredException
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import java.time.Instant

class QrExpValidatorTest : DescribeSpec({

    describe("QrExpValidator.validate()") {

        context("Given: 미래 초 단위 타임스탬프 (< 10,000,000,000)") {
            it("When: validate() 호출 시 Then: 예외 없이 통과한다") {
                val futureSeconds = (Instant.now().epochSecond + 3600L)
                shouldNotThrow<Exception> { QrExpValidator.validate(futureSeconds) }
            }
        }

        context("Given: 미래 밀리초 단위 타임스탬프 (>= 10,000,000,000)") {
            it("When: validate() 호출 시 Then: 예외 없이 통과한다") {
                val futureMillis = Instant.now().toEpochMilli() + 3_600_000L
                shouldNotThrow<Exception> { QrExpValidator.validate(futureMillis) }
            }
        }

        context("Given: 과거 초 단위 타임스탬프") {
            it("When: validate() 호출 시 Then: QrExpiredException이 발생한다") {
                // 1970-01-01 00:00:01 은 항상 과거
                shouldThrow<QrExpiredException> { QrExpValidator.validate(1L) }
            }
        }

        context("Given: 과거 밀리초 단위 타임스탬프") {
            it("When: validate() 호출 시 Then: QrExpiredException이 발생한다") {
                // 밀리초 단위지만 2001년 이전 → 항상 과거
                val pastMillis = 1_000_000_000_000L // 2001-09-09 (over 20 years ago)
                shouldThrow<QrExpiredException> { QrExpValidator.validate(pastMillis) }
            }
        }
    }
})
