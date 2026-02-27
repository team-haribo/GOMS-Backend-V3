package com.example.team.haribo.goms.domain.report.dto.response

import com.example.team.haribo.goms.domain.common.enums.Department
import com.example.team.haribo.goms.domain.common.enums.ReportReason
import com.example.team.haribo.goms.domain.common.enums.ReportStatus
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class ReportDetailResponse(

    @JsonProperty("report_id")
    val reportId: Long,

    @JsonProperty("reason")
    val reason: ReportReason,

    @JsonProperty("review_id")
    val reviewId: Long,

    @JsonProperty("review_created_at")
    val reviewCreatedAt: LocalDateTime,

    @JsonProperty("reviewer_member_id")
    val reviewerMemberId: Long,

    @JsonProperty("reviewer_name")
    val reviewerName: String,

    @JsonProperty("reviewer_grade")
    val reviewerGrade: Int,

    @JsonProperty("reviewer_department")
    val reviewerDepartment: Department,

    @JsonProperty("review_content")
    val reviewContent: String,

    @JsonProperty("report_content")
    val reportContent: String,

    @JsonProperty("report_created_at")
    val reportCreatedAt: LocalDateTime,

    @JsonProperty("report_status")
    val reportStatus: ReportStatus,

    @JsonProperty("deleted_at")
    val deletedAt: LocalDateTime?,

    @JsonProperty("deleted_by")
    val deletedBy: String?
)