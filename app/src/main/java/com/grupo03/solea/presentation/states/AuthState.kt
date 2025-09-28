package com.grupo03.solea.presentation.states

import com.grupo03.solea.data.models.User
import com.grupo03.solea.utils.ErrorCode


object AuthState {
    data class State(
        val isLoading: Boolean = false,
        val signInFormState: SignInFormState = SignInFormState(),
        val signUpFormState: SignUpFormState = SignUpFormState(),
        val user: User? = null,
        val errorCode: ErrorCode.Auth? = null,
    )

    enum class FormType {
        LOGIN,
        REGISTER
    }

    data class SignInFormState(
        val email: String = "",
        val password: String = "",
        val isEmailValid: Boolean = true,
    )

    data class SignUpFormState(
        val name: String = "",
        val email: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val isNameValid: Boolean = true,
        val isEmailValid: Boolean = true,
        val isPasswordValid: Boolean = true,
    )
}