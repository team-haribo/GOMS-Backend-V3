package com.teamharibo.goms.domain.outing.repository

import com.teamharibo.goms.domain.outing.entity.Outing
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
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
          and m.status = com.teamharibo.goms.domain.common.enums.Status.OUTING
        order by o.outingAt asc
        """
    )
    fun findAllActiveWithOutingMember(): List<Outing>

    /** 오래된 Outing/Member Entity를 영속성 컨텍스트에 올리지 않고 처리할 대상 Member ID만 조회합니다. */
    @Query(
        """
        select o.member.id from Outing o
        where o.comingAt is null
          and o.member.status = com.teamharibo.goms.domain.common.enums.Status.OUTING
        """
    )
    fun findAllActiveMemberIds(): List<Long>

    /** Member 행을 ID 오름차순으로 잠근 뒤 최신 상태의 Outing을 조회합니다. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        select o from Outing o
        join fetch o.member m
        where o.comingAt is null
          and m.status = com.teamharibo.goms.domain.common.enums.Status.OUTING
          and m.id in :memberIds
        order by m.id asc, o.id asc
        """
    )
    fun findAllActiveByMemberIdInForUpdate(@Param("memberIds") memberIds: List<Long>): List<Outing>

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
