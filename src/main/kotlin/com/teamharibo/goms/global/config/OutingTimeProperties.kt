package com.teamharibo.goms.global.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.LocalTime

@ConfigurationProperties(prefix = "outing.time")
data class OutingTimeProperties(
    val firstAdmissionYear: Int,
    val schoolYearStartMonth: Int,
    val schoolYearStartDay: Int,
    val startTimes: Map<Int, LocalTime>
)
