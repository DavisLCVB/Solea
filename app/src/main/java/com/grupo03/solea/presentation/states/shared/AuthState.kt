package com.grupo03.solea.presentation.states.shared

import com.grupo03.solea.data.models.User
import com.grupo03.solea.utils.AuthError

enum class FormType {
    SIGN_IN,
    SIGN_UP
}

data class AuthState(
    val user: User? = null,
    val errorCode: AuthError? = null,
)
