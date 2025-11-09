package com.grupo03.solea.presentation.states.screens

import com.grupo03.solea.data.models.SavingsGoal
import com.grupo03.solea.utils.AppError

data class SavingsState(
    val goals: List<SavingsGoal> = emptyList(),
    val isLoading: Boolean = false,
    val error: AppError? = null
)
