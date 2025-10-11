package com.grupo03.solea.presentation.states.screens

data class SignInFormState(
    val email: String = "",
    val password: String = "",
    val isEmailValid: Boolean = true,
    val isLoading: Boolean = false,
)