package com.example.team.haribo.goms.domain.late.repository

import com.example.team.haribo.goms.domain.late.entity.Late
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface LateRepository : JpaRepository<Late, Long> {

    @Query(
        """
        SELECT l
        FROM Late l
        JOIN FETCH l.member m
        WHERE l.comingAt >= :start AND l.comingAt < :end
        ORDER BY l.comingAt DESC
        """
    )
    fun findAllByComingAtRangeWithMember(
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): List<Late>
}
