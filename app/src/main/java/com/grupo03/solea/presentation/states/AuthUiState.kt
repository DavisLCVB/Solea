package com.grupo03.solea.presentation.states

import com.grupo03.solea.utils.ErrorCode

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorCode: ErrorCode.Auth? = null,
    val email: String = "",
    val password: String = "",
    val isEmailValid: Boolean = true,
    val isPasswordValid: Boolean = true
)