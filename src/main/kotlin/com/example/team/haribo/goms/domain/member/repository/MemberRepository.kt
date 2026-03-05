package com.example.team.haribo.goms.domain.member.repository

import com.example.team.haribo.goms.domain.common.enums.Department
import com.example.team.haribo.goms.domain.common.enums.Role
import com.example.team.haribo.goms.domain.common.enums.Status
import com.example.team.haribo.goms.domain.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface MemberRepository : JpaRepository<Member, Long> {

    fun findByEmail(email: String): Optional<Member>

    fun existsByEmail(email: String): Boolean

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
        @Param("status") status: Status?,
        @Param("role") role: Role?
    ): List<Member>

    fun findAllByStatus(status: com.example.team.haribo.goms.domain.common.enums.Status): List<com.example.team.haribo.goms.domain.member.entity.Member>
}
