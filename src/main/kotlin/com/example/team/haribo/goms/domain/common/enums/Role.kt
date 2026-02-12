package com.example.team.haribo.goms.domain.common.enums

import org.springframework.security.core.GrantedAuthority

enum class Role(
    private val authority: String
) : GrantedAuthority {

    ROLE_STUDENT("ROLE_STUDENT"),
    ROLE_STUDENT_COUNCIL("ROLE_STUDENT_COUNCIL");

    override fun getAuthority(): String = authority
}
