package com.example.team.haribo.goms.domain.late.service.impl

import com.example.team.haribo.goms.domain.late.dto.response.LateRankListResponse
import com.example.team.haribo.goms.domain.late.repository.LateRepository
import com.example.team.haribo.goms.domain.late.service.LateRankService
import com.example.team.haribo.goms.domain.studentcouncil.dto.response.LateStudentResponse
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class LateRankServiceImpl(
    private val lateRepository: LateRepository
) : LateRankService {

    override fun getTop5(): LateRankListResponse {
        val top5 = lateRepository.findLateRank(PageRequest.of(0, 5))

        return LateRankListResponse(
            students = top5.map {
                LateStudentResponse(
                    memberId = it.member.id!!,
                    name = it.member.name,
                    grade = it.member.grade,
                    department = it.member.department,
                    comingAt = it.comingAt
                )
            }
        )
    }
}
