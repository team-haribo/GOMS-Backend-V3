package com.example.team.haribo.goms.domain.review.repository

import com.example.team.haribo.goms.domain.review.entity.Review
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface ReviewRepository : JpaRepository<Review, Long> {

    @Query(
        """
        SELECT r
        FROM Review r
        JOIN FETCH r.member m
        WHERE r.place.id = :placeId
          AND r.deletedAt IS NULL
        ORDER BY r.createdAt DESC
        """
    )
    fun findAllActiveByPlaceId(placeId: Long): List<Review>

    @Query(
        """
        SELECT COUNT(r)
        FROM Review r
        WHERE r.place.id = :placeId
          AND r.deletedAt IS NULL
        """
    )
    fun countActiveByPlaceId(placeId: Long): Long

    @Query(
        """
        SELECT r
        FROM Review r
        WHERE r.id = :reviewId
          AND r.deletedAt IS NULL
        """
    )
    fun findActiveById(reviewId: Long): Optional<Review>

    @Query(
        """
        SELECT r
        FROM Review r
        WHERE r.id = :reviewId
        """
    )
    fun findAnyById(reviewId: Long): Optional<Review>

    fun existsByPlaceIdAndMemberIdAndDeletedAtIsNull(placeId: Long, memberId: Long): Boolean
}
