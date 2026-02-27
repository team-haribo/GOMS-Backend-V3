package com.example.team.haribo.goms.domain.report.entity

import com.example.team.haribo.goms.domain.common.enums.ReportReason
import com.example.team.haribo.goms.domain.common.enums.ReportStatus
import com.example.team.haribo.goms.domain.review.entity.Review
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(
    name = "review_report",
    uniqueConstraints = [UniqueConstraint(columnNames = ["review_id", "member_id"])]
)
class ReviewReport(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    val review: Review,

    @Column(name = "member_id", nullable = false)
    val memberId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val reason: ReportReason,

    @Column(nullable = false, length = 500)
    val content: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "report_status", nullable = false)
    var status: ReportStatus = ReportStatus.PENDING
) {

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    var createdAt: LocalDateTime? = null

    @Column(name = "resolved_at")
    var resolvedAt: LocalDateTime? = null

    @Column(name = "resolved_by")
    var resolvedBy: Long? = null

    fun isResolved(): Boolean = status != ReportStatus.PENDING

    fun resolve(status: ReportStatus, resolverId: Long) {
        this.status = status
        this.resolvedBy = resolverId
        this.resolvedAt = LocalDateTime.now()
    }
}