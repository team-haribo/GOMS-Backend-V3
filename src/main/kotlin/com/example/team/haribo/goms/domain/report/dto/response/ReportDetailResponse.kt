package com.example.team.haribo.goms.domain.report.dto.response

import com.example.team.haribo.goms.domain.common.enums.Department
import com.example.team.haribo.goms.domain.common.enums.ReportStatus
import java.time.LocalDateTime

data class ReportDetailResponse(
    val reportId: Long,
    val reviewId: Long,
    val reviewCreatedAt: LocalDateTime,
    val reviewerMemberId: Long,
    val reviewerName: String,
    val reviewerGrade: Int,
    val reviewerDepartment: Department,
    val reviewerProfileImageUrl: String?,
    val reviewContent: String,
    val reportContent: String,
    val placeName: String,
    val reportCreatedAt: LocalDateTime,
    val reportStatus: ReportStatus,
    val deletedAt: LocalDateTime?,
    val deletedBy: String?
)