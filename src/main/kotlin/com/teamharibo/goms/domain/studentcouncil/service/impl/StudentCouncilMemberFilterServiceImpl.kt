package com.teamharibo.goms.domain.studentcouncil.service.impl

import com.teamharibo.goms.domain.common.enums.Department
import com.teamharibo.goms.domain.common.enums.Gender
import com.teamharibo.goms.domain.common.enums.Role
import com.teamharibo.goms.domain.common.enums.Status
import com.teamharibo.goms.domain.member.repository.MemberRepository
import com.teamharibo.goms.domain.studentcouncil.dto.response.StudentResponse
import com.teamharibo.goms.domain.studentcouncil.dto.response.StudentsListResponse
import com.teamharibo.goms.domain.studentcouncil.service.StudentCouncilMemberFilterService
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
                    role = it.role,
                    status = it.status,
                    profileImageUrl = it.profileImageUrl
                )
            }
        )
    }
}