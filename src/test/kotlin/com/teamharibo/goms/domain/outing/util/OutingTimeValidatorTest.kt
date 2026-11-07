package com.teamharibo.goms.domain.outing.util

import com.teamharibo.goms.domain.outing.exception.OutingGradeNotAllowedException
import com.teamharibo.goms.domain.outing.exception.OutingTimeNotAllowedException
import com.teamharibo.goms.global.config.OutingTimeProperties
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import java.time.Clock
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class OutingTimeValidatorTest : DescribeSpec({

    val zone = ZoneId.of("Asia/Seoul")
    val properties = OutingTimeProperties(
        firstAdmissionYear = 2017,
        schoolYearStartMonth = 3,
        schoolYearStartDay = 1,
        startTimes = mapOf(
            1 to LocalTime.of(18, 50),
            2 to LocalTime.of(18, 45),
            3 to LocalTime.of(18, 40)
        )
    )

    fun validatorAt(dateTime: LocalDateTime) =
        OutingTimeValidator(Clock.fixed(dateTime.atZone(zone).toInstant(), zone), properties)

    describe("OutingTimeValidator.validate()") {

        context("Given: 2026학년도 3학년(8기)") {
            it("When: 시작 1초 전(18:39:59) Then: OutingTimeNotAllowedException이 발생한다") {
                shouldThrow<OutingTimeNotAllowedException> {
                    validatorAt(LocalDateTime.of(2026, 11, 4, 18, 39, 59)).validate(8)
                }
            }

            it("When: 시작 정각(18:40:00) Then: 예외 없이 통과한다") {
                shouldNotThrowAny {
                    validatorAt(LocalDateTime.of(2026, 11, 4, 18, 40, 0)).validate(8)
                }
            }

            it("When: 시작 이후 늦은 시각(23:00) Then: 예외 없이 통과한다") {
                shouldNotThrowAny {
                    validatorAt(LocalDateTime.of(2026, 11, 4, 23, 0)).validate(8)
                }
            }
        }

        context("Given: 2026학년도 2학년(9기)") {
            it("When: 시작 1초 전(18:44:59) Then: OutingTimeNotAllowedException이 발생한다") {
                shouldThrow<OutingTimeNotAllowedException> {
                    validatorAt(LocalDateTime.of(2026, 11, 4, 18, 44, 59)).validate(9)
                }
            }

            it("When: 시작 정각(18:45:00) Then: 예외 없이 통과한다") {
                shouldNotThrowAny {
                    validatorAt(LocalDateTime.of(2026, 11, 4, 18, 45, 0)).validate(9)
                }
            }
        }

        context("Given: 2026학년도 1학년(10기)") {
            it("When: 시작 1초 전(18:49:59) Then: OutingTimeNotAllowedException이 발생한다") {
                shouldThrow<OutingTimeNotAllowedException> {
                    validatorAt(LocalDateTime.of(2026, 11, 4, 18, 49, 59)).validate(10)
                }
            }

            it("When: 시작 정각(18:50:00) Then: 예외 없이 통과한다") {
                shouldNotThrowAny {
                    validatorAt(LocalDateTime.of(2026, 11, 4, 18, 50, 0)).validate(10)
                }
            }
        }

        context("Given: 학년도 전환 경계의 9기") {
            it("When: 2027-02-28 18:44(2학년 시작 전) Then: OutingTimeNotAllowedException이 발생한다") {
                shouldThrow<OutingTimeNotAllowedException> {
                    validatorAt(LocalDateTime.of(2027, 2, 28, 18, 44)).validate(9)
                }
            }

            it("When: 2027-03-01 18:44(3학년 시작 이후) Then: 예외 없이 통과한다") {
                shouldNotThrowAny {
                    validatorAt(LocalDateTime.of(2027, 3, 1, 18, 44)).validate(9)
                }
            }
        }

        context("Given: 재학 범위를 벗어난 기수") {
            it("When: 2027-02-28의 8기(3학년) Then: 예외 없이 통과한다") {
                shouldNotThrowAny {
                    validatorAt(LocalDateTime.of(2027, 2, 28, 20, 0)).validate(8)
                }
            }

            it("When: 2027-03-01의 8기(졸업) Then: OutingGradeNotAllowedException이 발생한다") {
                shouldThrow<OutingGradeNotAllowedException> {
                    validatorAt(LocalDateTime.of(2027, 3, 1, 20, 0)).validate(8)
                }
            }

            it("When: 2026학년도의 11기(입학 전) Then: OutingGradeNotAllowedException이 발생한다") {
                shouldThrow<OutingGradeNotAllowedException> {
                    validatorAt(LocalDateTime.of(2026, 11, 4, 20, 0)).validate(11)
                }
            }
        }
    }
})
