package com.example.team.haribo.goms.domain.auth.util

import com.example.team.haribo.goms.domain.auth.exception.InvalidEmailFormatException
import com.example.team.haribo.goms.domain.auth.exception.InvalidPasswordPolicyException

object AuthValidators {

    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

    private val passwordRegex =
        Regex("^(?=.*[a-zA-Z])[A-Za-z0-9!@#\$%^&?~*]{6,15}$")

    fun validateEmail(email: String) {
        if (!emailRegex.matches(email)) throw InvalidEmailFormatException()
    }

    fun validatePassword(password: String) {
        if (!passwordRegex.matches(password)) throw InvalidPasswordPolicyException()
    }
}
