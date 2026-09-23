package com.teamharibo.goms.domain.outing.service.impl

import com.teamharibo.goms.domain.outing.dto.response.OutingStudentListResponse
import com.teamharibo.goms.domain.outing.dto.response.OutingStudentResponse
import com.teamharibo.goms.domain.outing.exception.EmptyNameException
import com.teamharibo.goms.domain.outing.repository.OutingRepository
import com.teamharibo.goms.domain.outing.service.OutingStudentSearchService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OutingStudentSearchServiceImpl(
    private val outingRepository: OutingRepository
) : OutingStudentSearchService {

    @Transactional(readOnly = true)
    override fun search(name: String?): OutingStudentListResponse {
        if (name.isNullOrBlank()) {
            throw EmptyNameException()
        }

        val outings = outingRepository.searchActiveWithMemberByName(name)

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