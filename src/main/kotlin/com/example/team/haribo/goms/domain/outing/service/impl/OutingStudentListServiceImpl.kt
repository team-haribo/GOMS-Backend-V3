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
