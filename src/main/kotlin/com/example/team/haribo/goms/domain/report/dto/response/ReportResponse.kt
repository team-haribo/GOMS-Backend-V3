package com.example.team.haribo.goms.domain.report.dto.response

import com.example.team.haribo.goms.domain.common.enums.Department
import com.example.team.haribo.goms.domain.common.enums.ReportStatus
import com.example.team.haribo.goms.domain.report.entity.ReviewReport
import java.time.LocalDateTime

data class ReportResponse(
    val reportId: Long,
    val reviewId: Long,
    val reviewerMemberId: Long,
    val reviewerName: String,
    val reviewerGrade: Int,
    val reviewerDepartment: Department,
    val reviewerProfileImageUrl: String?,
    val reportCreatedAt: LocalDateTime,
    val reportStatus: ReportStatus,
    val deletedAt: LocalDateTime?,
    val deletedBy: String?,
)