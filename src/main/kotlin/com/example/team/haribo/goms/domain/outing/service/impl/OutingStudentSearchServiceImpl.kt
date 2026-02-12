package com.example.team.haribo.goms.domain.outing.service.impl

import com.example.team.haribo.goms.domain.outing.dto.response.OutingStudentListResponse
import com.example.team.haribo.goms.domain.outing.dto.response.OutingStudentResponse
import com.example.team.haribo.goms.domain.outing.exception.EmptyNameException
import com.example.team.haribo.goms.domain.outing.repository.OutingRepository
import com.example.team.haribo.goms.domain.outing.service.OutingStudentSearchService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OutingStudentSearchServiceImpl(
    private val outingRepository: OutingRepository
) : OutingStudentSearchService {

    @Transactional(readOnly = true)
    override fun search(name: String?): OutingStudentListResponse {
        val keyword = name?.trim()
        if (keyword.isNullOrEmpty()) {
            throw EmptyNameException()
        }

        val outings = outingRepository.searchActiveWithMemberByName(keyword)
        val students = outings.map {
            OutingStudentResponse(
                name = it.member.name,
                grade = it.member.grade.toLong(),
                department = it.member.department.name,
                outingAt = it.outingAt
            )
        }
        return OutingStudentListResponse(students)
    }
}
