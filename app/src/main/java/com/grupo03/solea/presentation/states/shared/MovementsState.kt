package com.grupo03.solea.presentation.states.shared

import com.grupo03.solea.data.models.Category
import com.grupo03.solea.data.models.ExpenseDetails
import com.grupo03.solea.data.models.IncomeDetails
import com.grupo03.solea.utils.AppError


data class MovementsState(
    val incomeDetailsList: List<IncomeDetails> = emptyList(),
    val expenseDetailsList: List<ExpenseDetails> = emptyList(),
    val categoriesList: List<Category> = emptyList(),
    val error: AppError? = null,
)
