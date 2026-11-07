package com.teamharibo.goms.domain.outing.util

import com.teamharibo.goms.domain.outing.exception.OutingGradeNotAllowedException
import com.teamharibo.goms.domain.outing.exception.OutingTimeNotAllowedException
import com.teamharibo.goms.global.config.OutingTimeProperties
import com.teamharibo.goms.global.log.LogFormat
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime

@Component
class OutingTimeValidator(
    private val clock: Clock,
    private val properties: OutingTimeProperties
) {

    private val log = LoggerFactory.getLogger(OutingTimeValidator::class.java)

    fun validate(generation: Int) {
        val now = LocalDateTime.now(clock)

        val schoolYearStart = LocalDate.of(now.year, properties.schoolYearStartMonth, properties.schoolYearStartDay)
        val schoolYear = if (now.toLocalDate().isBefore(schoolYearStart)) now.year - 1 else now.year

        val admissionYear = properties.firstAdmissionYear + (generation - 1)
        val grade = schoolYear - admissionYear + 1

        val startTime = properties.startTimes[grade]
        if (startTime == null) {
            log.warn(
                LogFormat.message(
                    domain = "OUTING",
                    event = "외출 실패",
                    "reason" to "외출 가능 학년 아님",
                    "generation" to generation,
                    "grade" to grade,
                    "schoolYear" to schoolYear
                )
            )
            throw OutingGradeNotAllowedException()
        }

        if (now.toLocalTime().isBefore(startTime)) {
            log.warn(
                LogFormat.message(
                    domain = "OUTING",
                    event = "외출 실패",
                    "reason" to "외출 시작 시각 전",
                    "generation" to generation,
                    "grade" to grade,
                    "startTime" to startTime,
                    "now" to now
                )
            )
            throw OutingTimeNotAllowedException()
        }
    }
}