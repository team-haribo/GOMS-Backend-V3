package com.example.team.haribo.goms.domain.studentcouncil.service.impl

import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.domain.studentcouncil.dto.response.StudentResponse
import com.example.team.haribo.goms.domain.studentcouncil.dto.response.StudentsListResponse
import com.example.team.haribo.goms.domain.studentcouncil.service.StudentCouncilMemberListService
import org.springframework.stereotype.Service

@Service
class StudentCouncilMemberListServiceImpl(
    private val memberRepository: MemberRepository
) : StudentCouncilMemberListService {

    override fun list(): StudentsListResponse {
        val members = memberRepository.findAllSorted()

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