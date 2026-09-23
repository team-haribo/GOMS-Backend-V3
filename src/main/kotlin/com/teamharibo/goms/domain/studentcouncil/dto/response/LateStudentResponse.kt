package com.teamharibo.goms.domain.studentcouncil.dto.response

import com.teamharibo.goms.domain.common.enums.Department
import com.teamharibo.goms.domain.common.enums.Role
import com.teamharibo.goms.domain.common.enums.Status
import java.time.LocalDateTime

data class LateStudentResponse(
    val memberId: Long,
    val name: String,
    val grade: Int,
    val department: Department,
    val role: Role,
    val status: Status,
    val profileImageUrl: String?,
    val comingAt: LocalDateTime
)