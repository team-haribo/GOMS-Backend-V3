package com.example.team.haribo.goms.domain.studentcouncil.service.impl

import com.example.team.haribo.goms.domain.common.enums.Department
import com.example.team.haribo.goms.domain.common.enums.Gender
import com.example.team.haribo.goms.domain.common.enums.Role
import com.example.team.haribo.goms.domain.common.enums.Status
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.domain.studentcouncil.dto.response.StudentResponse
import com.example.team.haribo.goms.domain.studentcouncil.dto.response.StudentsListResponse
import com.example.team.haribo.goms.domain.studentcouncil.service.StudentCouncilMemberFilterService
import org.springframework.stereotype.Service

@Service
class StudentCouncilMemberFilterServiceImpl(
    private val memberRepository: MemberRepository
) : StudentCouncilMemberFilterService {

    override fun filter(
        name: String?,
        grade: Int?,
        department: Department?,
        gender: Gender?,
        status: Status?,
        role: Role?
    ): StudentsListResponse {
        val keyword = name?.trim()?.takeIf { it.isNotBlank() }

        val members = memberRepository.filterSorted(
            name = keyword,
            grade = grade,
            department = department,
            gender = gender,
            status = status,
            role = role
        )

        return StudentsListResponse(
            students = members.map {
                StudentResponse(
                    memberId = it.id!!,
                    name = it.name,
                    grade = it.grade,
                    department = it.department,
                    gender = it.gender
                )
            }
        )
    }
}