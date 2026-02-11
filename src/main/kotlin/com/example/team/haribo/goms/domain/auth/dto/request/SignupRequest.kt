package com.example.team.haribo.goms.domain.auth.dto.request

import com.example.team.haribo.goms.domain.common.enums.Department
import com.example.team.haribo.goms.domain.common.enums.Gender

data class SignupRequest(
    val email: String,
    val verifiedToken: String,
    val password: String,
    val name: String,
    val grade: Long,
    val department: Department,
    val gender: Gender
)
