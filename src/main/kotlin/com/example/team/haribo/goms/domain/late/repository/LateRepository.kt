package com.example.team.haribo.goms.domain.late.repository

import com.example.team.haribo.goms.domain.late.entity.Late
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface LateRepository : JpaRepository<Late, Long> {

    @Query(
        """
        SELECT l
        FROM Late l
        JOIN FETCH l.member m
        ORDER BY
            l.lateCount DESC,
            m.grade ASC,
            CASE
                WHEN m.department = com.example.team.haribo.goms.domain.common.enums.Department.SW THEN 0
                WHEN m.department = com.example.team.haribo.goms.domain.common.enums.Department.IOT THEN 1
                ELSE 2
            END,
            m.name ASC
        """
    )
    fun findLateRank(pageable: Pageable): List<Late>

    @Query(
        """
        SELECT l
        FROM Late l
        JOIN FETCH l.member m
        WHERE l.comingAt >= :start AND l.comingAt < :end
        ORDER BY l.comingAt DESC
        """
    )
    fun findAllByComingAtRangeWithMember(start: LocalDateTime, end: LocalDateTime): List<Late>

    fun countByMemberId(memberId: Long): Long

    @Query(
        """
        SELECT l.outing.id
        FROM Late l
        WHERE l.outing.id IN :outingIds
        """
    )
    fun findAllOutingIdsIn(outingIds: List<Long>): List<Long>

    @Query(
        """
        SELECT l.member.id AS memberId, COUNT(l) AS lateCount
        FROM Late l
        WHERE l.member.id IN :memberIds
        GROUP BY l.member.id
        """
    )
    fun countByMemberIds(memberIds: List<Long>): List<MemberLateCount>

    fun deleteAllByMemberId(memberId: Long): Long
}

interface MemberLateCount {
    val memberId: Long
    val lateCount: Long
}
