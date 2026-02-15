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
        select m from Member m
        order by m.grade asc,
          case
            when m.department = com.example.team.haribo.goms.domain.common.enums.Department.SW then 0
            when m.department = com.example.team.haribo.goms.domain.common.enums.Department.IOT then 1
            else 2
          end,
          m.name asc
        """
    )
    fun findAllSorted(): List<Member>

    @Query(
        """
        select m from Member m
        where lower(m.name) like lower(concat('%', :name, '%'))
        order by m.grade asc,
          case
            when m.department = com.example.team.haribo.goms.domain.common.enums.Department.SW then 0
            when m.department = com.example.team.haribo.goms.domain.common.enums.Department.IOT then 1
            else 2
          end,
          m.name asc
        """
    )
    fun searchByNameSorted(@Param("name") name: String): List<Member>

    @Query(
        """
        select m from Member m
        where (:name is null or lower(m.name) like lower(concat('%', :name, '%')))
          and (:grade is null or m.grade = :grade)
          and (:department is null or m.department = :department)
          and (:status is null or m.status = :status)
          and (:role is null or m.role = :role)
        order by m.grade asc,
          case
            when m.department = com.example.team.haribo.goms.domain.common.enums.Department.SW then 0
            when m.department = com.example.team.haribo.goms.domain.common.enums.Department.IOT then 1
            else 2
          end,
          m.name asc
        """
    )
    fun filterSorted(
        @Param("name") name: String?,
        @Param("grade") grade: Int?,
        @Param("department") department: Department?,
        @Param("status") status: Status?,
        @Param("role") role: Role?
    ): List<Member>
}
