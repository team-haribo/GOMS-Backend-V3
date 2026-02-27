package com.example.team.haribo.goms.domain.report.repository

import com.example.team.haribo.goms.domain.common.enums.ReportStatus
import com.example.team.haribo.goms.domain.report.entity.ReviewReport
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface ReviewReportRepository : JpaRepository<ReviewReport, Long> {

    fun existsByReview_IdAndMemberId(reviewId: Long, memberId: Long): Boolean

    @Query(
        """
        SELECT rr
        FROM ReviewReport rr
        JOIN FETCH rr.review r
        JOIN FETCH r.member m
        WHERE rr.memberId = :memberId
        ORDER BY rr.createdAt DESC
        """
    )
    fun findAllByMemberIdWithReviewAndReviewer(memberId: Long): List<ReviewReport>

    @Query(
        """
        SELECT rr
        FROM ReviewReport rr
        JOIN FETCH rr.review r
        JOIN FETCH r.member m
        WHERE rr.id = :reportId
        """
    )
    fun findByIdWithReviewAndReviewer(reportId: Long): Optional<ReviewReport>

    @Query(
        """
        SELECT rr
        FROM ReviewReport rr
        JOIN FETCH rr.review r
        JOIN FETCH r.member m
        WHERE rr.status = :status
        ORDER BY rr.createdAt DESC
        """
    )
    fun findAllByStatusWithReviewAndReviewer(status: ReportStatus): List<ReviewReport>

    @Query(
        """
        SELECT rr
        FROM ReviewReport rr
        JOIN FETCH rr.review r
        JOIN FETCH r.member m
        WHERE rr.status IN :statuses
        ORDER BY rr.createdAt DESC
        """
    )
    fun findAllByStatusInWithReviewAndReviewer(statuses: List<ReportStatus>): List<ReviewReport>
}