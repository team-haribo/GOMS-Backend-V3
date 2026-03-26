package com.example.team.haribo.goms.domain.report.dto.request

import com.example.team.haribo.goms.domain.common.enums.ReportStatus
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotNull

data class ReportResolveRequest(

    @JsonProperty("report_status")
    @field:NotNull(message = "reportStatus 는 필수입니다.")
    val reportStatus: ReportStatus
)