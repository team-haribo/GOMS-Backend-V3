package com.example.team.haribo.goms.domain.member.repository

import com.example.team.haribo.goms.domain.common.enums.Department
import com.example.team.haribo.goms.domain.common.enums.Gender
import com.example.team.haribo.goms.domain.common.enums.Role
import com.example.team.haribo.goms.domain.common.enums.Status
import com.example.team.haribo.goms.domain.member.entity.Member
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface MemberRepository : JpaRepository<Member, Long> {

    fun findByEmail(email: String): Optional<Member>

    fun existsByEmail(email: String): Boolean

    /**
     * 외출/복귀 상태 전이(QR 외출·복귀, 학생회 강제 외출·복귀)처럼
     * "조회 -> 상태 검증 -> 변경" 흐름이 동시에 들어올 수 있는 케이스 전용 조회.
     * 커밋 전까지 동일 회원에 대한 다른 트랜잭션의 갱신을 막아 중복 Outing 생성을 방지한다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Member m WHERE m.id = :id")
    fun findByIdForUpdate(@Param("id") id: Long): Member?

    @Query(
        """
        SELECT m
        FROM Member m
        ORDER BY
            m.grade ASC,
            CASE
                WHEN m.department = com.example.team.haribo.goms.domain.common.enums.Department.SW  THEN 0
                WHEN m.department = com.example.team.haribo.goms.domain.common.enums.Department.IOT THEN 1
                ELSE 2
            END,
            m.name ASC
        """
    )
    fun findAllSorted(): List<Member>

    @Query(
        """
        SELECT m
        FROM Member m
        WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))
        ORDER BY
            m.grade ASC,
            CASE
                WHEN m.department = com.example.team.haribo.goms.domain.common.enums.Department.SW  THEN 0
                WHEN m.department = com.example.team.haribo.goms.domain.common.enums.Department.IOT THEN 1
                ELSE 2
            END,
            m.name ASC
        """
    )
    fun searchByNameSorted(@Param("name") name: String): List<Member>

    @Query(
        """
        SELECT m
        FROM Member m
        WHERE (:name IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%')))
          AND (:grade IS NULL OR m.grade = :grade)
          AND (:department IS NULL OR m.department = :department)
          AND (:gender IS NULL OR m.gender = :gender)
          AND (:status IS NULL OR m.status = :status)
          AND (:role IS NULL OR m.role = :role)
        ORDER BY
            m.grade ASC,
            CASE
                WHEN m.department = com.example.team.haribo.goms.domain.common.enums.Department.SW  THEN 0
                WHEN m.department = com.example.team.haribo.goms.domain.common.enums.Department.IOT THEN 1
                ELSE 2
            END,
            m.name ASC
        """
    )
    fun filterSorted(
        @Param("name") name: String?,
        @Param("grade") grade: Int?,
        @Param("department") department: Department?,
        @Param("gender") gender: Gender?,
        @Param("status") status: Status?,
        @Param("role") role: Role?
    ): List<Member>

    fun findAllByStatus(status: Status): List<Member>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
        UPDATE Member m
        SET m.role = com.example.team.haribo.goms.domain.common.enums.Role.ROLE_STUDENT
        WHERE m.role = com.example.team.haribo.goms.domain.common.enums.Role.ROLE_STUDENT_COUNCIL
          AND m.isFixedStudentCouncil = false
        """
    )
    fun resetTemporaryStudentCouncilRole(): Int
}