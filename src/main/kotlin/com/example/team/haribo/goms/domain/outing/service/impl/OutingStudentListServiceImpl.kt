package com.example.team.haribo.goms.domain.outing.service.impl

import com.example.team.haribo.goms.domain.outing.dto.response.OutingStudentListResponse
import com.example.team.haribo.goms.domain.outing.dto.response.OutingStudentResponse
import com.example.team.haribo.goms.domain.outing.repository.OutingRepository
import com.example.team.haribo.goms.domain.outing.service.OutingStudentListService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OutingStudentListServiceImpl(
    private val outingRepository: OutingRepository
) : OutingStudentListService {

    @Transactional(readOnly = true)
    override fun getList(): OutingStudentListResponse {
        val outings = outingRepository.findAllActiveWithMember()

        return OutingStudentListResponse(
            students = outings.map {
                OutingStudentResponse(
                    memberId = requireNotNull(it.member.id) { "member.id must not be null" },
                    name = it.member.name,
                    grade = it.member.grade,
                    department = it.member.department.name,
                    role = it.member.role,
                    status = it.member.status,
                    profileImageUrl = it.member.profileImageUrl,
                    outingAt = it.outingAt
                )
            }
        )
    }
}