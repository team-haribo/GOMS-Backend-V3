package com.teamharibo.goms.domain.studentcouncil.service.impl

import com.teamharibo.goms.domain.late.repository.LateRepository
import com.teamharibo.goms.domain.studentcouncil.dto.response.LateStudentResponse
import com.teamharibo.goms.domain.studentcouncil.dto.response.LateStudentsListResponse
import com.teamharibo.goms.domain.studentcouncil.service.StudentCouncilLateListService
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class StudentCouncilLateListServiceImpl(
    private val lateRepository: LateRepository
) : StudentCouncilLateListService {

    override fun list(date: LocalDate?): LateStudentsListResponse {
        val target = date ?: LocalDate.now()
        val start = target.atStartOfDay()
        val end = target.plusDays(1).atStartOfDay()

        val lates = lateRepository.findAllByComingAtRangeWithMember(start, end)

        return LateStudentsListResponse(
            students = lates.map {
                LateStudentResponse(
                    memberId = it.member.id!!,
                    name = it.member.name,
                    grade = it.member.grade,
                    department = it.member.department,
                    role = it.member.role,
                    status = it.member.status,
                    profileImageUrl = it.member.profileImageUrl,
                    comingAt = it.comingAt
                )
            }
        )
    }
}