package com.example.team.haribo.goms.domain.place.repository

import com.example.team.haribo.goms.domain.place.entity.PlaceRecommend
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime
import java.util.Optional

interface PlaceRecommendRepository : JpaRepository<PlaceRecommend, Long> {

    interface PlaceRecommendCountProjection {
        val placeId: Long
        val recommendCount: Long
    }

    fun findByPlaceIdAndMemberId(placeId: Long, memberId: Long): Optional<PlaceRecommend>

    fun countByPlaceIdAndRecommendedTrue(placeId: Long): Long

    fun countByMemberIdAndRecommendedTrue(memberId: Long): Long

    fun findAllByMemberIdAndRecommendedTrue(memberId: Long): List<PlaceRecommend>

    fun deleteAllByMember_Id(memberId: Long): Long

    @Query(
        """
        SELECT pr.place.id
        FROM PlaceRecommend pr
        WHERE pr.member.id = :memberId AND pr.recommended = true
        """
    )
    fun findRecommendedPlaceIds(memberId: Long): List<Long>

    @Query(
        """
        SELECT pr.place.id
        FROM PlaceRecommend pr
        WHERE pr.recommended = true
        GROUP BY pr.place.id
        ORDER BY COUNT(pr.id) DESC
        """
    )
    fun findHotPlaceIds(pageable: Pageable): List<Long>

    @Query(
        """
        SELECT pr.place.id AS placeId, COUNT(pr.id) AS recommendCount
        FROM PlaceRecommend pr
        WHERE pr.recommended = true
          AND pr.place.id IN :placeIds
        GROUP BY pr.place.id
        """
    )
    fun countRecommendedByPlaceIds(placeIds: List<Long>): List<PlaceRecommendCountProjection>

    @Query(
        """
        SELECT pr.place.id
        FROM PlaceRecommend pr
        WHERE pr.recommended = true
          AND pr.createdAt >= :since
        GROUP BY pr.place.id
        ORDER BY COUNT(pr.id) DESC
        """
    )
    fun findHotPlaceIdsSince(since: LocalDateTime, pageable: Pageable): List<Long>

    @Query(
        """
        SELECT pr.place.id AS placeId, COUNT(pr.id) AS recommendCount
        FROM PlaceRecommend pr
        WHERE pr.recommended = true
          AND pr.createdAt >= :since
          AND pr.place.id IN :placeIds
        GROUP BY pr.place.id
        """
    )
    fun countRecommendedByPlaceIdsSince(placeIds: List<Long>, since: LocalDateTime): List<PlaceRecommendCountProjection>
}