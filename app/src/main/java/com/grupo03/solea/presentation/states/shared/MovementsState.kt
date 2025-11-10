package com.grupo03.solea.presentation.states.shared

import com.grupo03.solea.data.models.Category
import com.grupo03.solea.data.models.ExpenseDetails
import com.grupo03.solea.data.models.IncomeDetails
import com.grupo03.solea.data.models.SaveDetails
import com.grupo03.solea.utils.AppError

/**
 * UI state for financial movements (incomes and expenses).
 *
 * This state aggregates all movement-related data needed by multiple screens,
 * including complete details of incomes and expenses, as well as available categories.
 *
 * @property incomeDetailsList List of all income transactions with complete details
 * @property expenseDetailsList List of all expense transactions with complete details (including sources)
 * @property categoriesList List of available categories (both default and user-created)
 * @property error Error that occurred during data fetching, null if no error
 */
data class MovementsState(
    val incomeDetailsList: List<IncomeDetails> = emptyList(),
    val expenseDetailsList: List<ExpenseDetails> = emptyList(),
    val saveDetailsList: List<SaveDetails> = emptyList(),
    val categoriesList: List<Category> = emptyList(),
    val balance: Double = 0.0,
    val error: AppError? = null,
)
