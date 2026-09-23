package com.teamharibo.goms.domain.studentcouncil.service.impl

import com.teamharibo.goms.domain.member.repository.MemberRepository
import com.teamharibo.goms.domain.outing.exception.EmptyNameException
import com.teamharibo.goms.domain.studentcouncil.dto.response.StudentResponse
import com.teamharibo.goms.domain.studentcouncil.dto.response.StudentSearchResponse
import com.teamharibo.goms.domain.studentcouncil.service.StudentCouncilMemberSearchService
import org.springframework.stereotype.Service

@Service
class StudentCouncilMemberSearchServiceImpl(
    private val memberRepository: MemberRepository
) : StudentCouncilMemberSearchService {

    override fun search(name: String?): StudentSearchResponse {
        val keyword = name?.trim()
        if (keyword.isNullOrBlank()) throw EmptyNameException()

        val members = memberRepository.searchByNameSorted(keyword)

        return StudentSearchResponse(
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