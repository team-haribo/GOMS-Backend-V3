package com.example.team.haribo.goms.domain.place.repository

import com.example.team.haribo.goms.domain.place.entity.PlaceRecommend
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface PlaceRecommendRepository : JpaRepository<PlaceRecommend, Long> {

    fun findByPlaceIdAndMemberId(placeId: Long, memberId: Long): Optional<PlaceRecommend>

    fun countByPlaceIdAndRecommendedTrue(placeId: Long): Long

    fun countByMemberIdAndRecommendedTrue(memberId: Long): Long

    fun findAllByMemberIdAndRecommendedTrue(memberId: Long): List<PlaceRecommend>

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
}
