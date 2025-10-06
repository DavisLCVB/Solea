package com.grupo03.solea.presentation.states.screens

import com.grupo03.solea.utils.AppError

data class NewCategoryFormState(
    val name: String = "",
    val description: String = "",
    val isNameValid: Boolean = true,
    val isLoading: Boolean = false,
    val error: AppError? = null,
    val successMessage: String? = null
)
