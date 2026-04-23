package com.example.team.haribo.goms.domain.outing.repository

import com.example.team.haribo.goms.domain.outing.entity.Outing
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface OutingRepository : JpaRepository<Outing, Long> {

    fun findTopByMemberIdAndComingAtIsNullOrderByIdDesc(memberId: Long): Outing?

    fun deleteAllByMember_Id(memberId: Long): Long

    @Query(
        """
        select o from Outing o
        join fetch o.member m
        where o.comingAt is null
        order by o.outingAt desc
        """
    )
    fun findAllActiveWithMember(): List<Outing>

    @Query(
        """
        select o from Outing o
        join fetch o.member m
        where o.comingAt is null
          and lower(m.name) like lower(concat('%', :name, '%'))
        order by o.outingAt desc
        """
    )
    fun searchActiveWithMemberByName(@Param("name") name: String): List<Outing>

    @Query(
        """
        select count(o) from Outing o
        where o.comingAt is null
        """
    )
    fun countActive(): Long
}