package com.example.team.haribo.goms.domain.report.dto.response

import com.example.team.haribo.goms.domain.common.enums.Department
import com.example.team.haribo.goms.domain.common.enums.ReportReason
import com.example.team.haribo.goms.domain.common.enums.ReportStatus
import java.time.LocalDateTime

data class ReportDetailResponse(
    val report_id: Long,
    val reason: ReportReason,
    val review_id: Long,
    val review_created_at: LocalDateTime,
    val reviewer_member_id: Long,
    val reviewer_name: String,
    val reviewer_grade: Int,
    val reviewer_department: Department,
    val review_content: String,
    val report_content: String,
    val report_created_at: LocalDateTime,
    val report_status: ReportStatus,
    val deleted_at: LocalDateTime?,
    val deleted_by: String?
)