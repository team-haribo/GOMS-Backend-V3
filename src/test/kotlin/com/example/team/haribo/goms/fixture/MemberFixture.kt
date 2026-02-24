package com.example.team.haribo.goms.fixture

import com.example.team.haribo.goms.domain.common.enums.Department
import com.example.team.haribo.goms.domain.common.enums.Gender
import com.example.team.haribo.goms.domain.common.enums.Role
import com.example.team.haribo.goms.domain.common.enums.Status
import com.example.team.haribo.goms.domain.member.entity.Member

object MemberFixture {

    fun student(id: Long = 1L, status: Status = Status.COMING) = Member(
        email = "student@gsm.hs.kr",
        password = "encoded_password",
        name = "홍길동",
        grade = 1,
        department = Department.SW,
        gender = Gender.MALE,
        role = Role.ROLE_STUDENT,
        status = status
    ).also { it.id = id }

    fun council(id: Long = 2L, status: Status = Status.COMING) = Member(
        email = "council@gsm.hs.kr",
        password = "encoded_password",
        name = "학생회장",
        grade = 2,
        department = Department.IOT,
        gender = Gender.FEMALE,
        role = Role.ROLE_STUDENT_COUNCIL,
        status = status
    ).also { it.id = id }

    fun cannotOuting(id: Long = 3L) = student(id = id, status = Status.CANNOT_OUTING)

    fun outing(id: Long = 4L) = student(id = id, status = Status.OUTING)
}
