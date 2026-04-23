package com.example.team.haribo.goms.domain.review.repository

import com.example.team.haribo.goms.domain.review.entity.Review
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ReviewRepository : JpaRepository<Review, Long> {

    interface PlaceReviewCountProjection {
        val placeId: Long
        val reviewCount: Long
    }

    fun existsByPlaceIdAndMemberIdAndDeletedAtIsNull(placeId: Long, memberId: Long): Boolean

    fun deleteAllByMember_Id(memberId: Long): Long

    fun countAllByMemberIdAndDeletedAtIsNull(memberId: Long): Long

    @Query(
        """
        SELECT r
        FROM Review r
        JOIN FETCH r.member
        WHERE r.place.id = :placeId
          AND r.deletedAt IS NULL
        ORDER BY r.createdAt DESC
        """
    )
    fun findAllActiveByPlaceId(placeId: Long): List<Review>

    @Query(
        """
        SELECT r
        FROM Review r
        JOIN FETCH r.place
        WHERE r.member.id = :memberId
          AND r.deletedAt IS NULL
        ORDER BY r.createdAt DESC
        """
    )
    fun findAllActiveByMemberId(memberId: Long): List<Review>

    @Query(
        """
        SELECT COUNT(r.id)
        FROM Review r
        WHERE r.place.id = :placeId
          AND r.deletedAt IS NULL
        """
    )
    fun countActiveByPlaceId(placeId: Long): Long

    @Query(
        """
        SELECT r.place.id AS placeId, COUNT(r.id) AS reviewCount
        FROM Review r
        WHERE r.deletedAt IS NULL
          AND r.place.id IN :placeIds
        GROUP BY r.place.id
        """
    )
    fun countActiveByPlaceIds(placeIds: List<Long>): List<PlaceReviewCountProjection>
}