package com.example.team.haribo.goms.domain.late.repository

import com.example.team.haribo.goms.domain.late.entity.Late
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface LateRepository : JpaRepository<Late, Long> {

    @Query(
        """
        select l from Late l
        join fetch l.member m
        where l.comingAt >= :start and l.comingAt < :end
        order by l.comingAt desc
        """
    )
    fun findAllByComingAtRangeWithMember(
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): List<Late>
}
