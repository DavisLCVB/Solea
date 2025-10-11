package com.grupo03.solea.presentation.states.screens

import com.grupo03.solea.data.models.ExpenseDetails
import com.grupo03.solea.data.models.IncomeDetails
import com.grupo03.solea.data.models.Movement
import com.grupo03.solea.data.models.MovementType
import com.grupo03.solea.utils.AppError
import java.time.LocalDate

enum class DateFilter {
    TODAY,      // HOY
    WEEK,       // SEMANA
    MONTH,      // MES
    YEAR,       // AÑO
    CUSTOM      // PERS.
}

/**
 * Unified movement representation for history display
 */
sealed class HistoryMovementItem {
    abstract val id: String
    abstract val datetime: java.time.LocalDateTime

    data class IncomeItem(
        override val id: String,
        override val datetime: java.time.LocalDateTime,
        val incomeDetails: IncomeDetails
    ) : HistoryMovementItem()

    data class ExpenseItem(
        override val id: String,
        override val datetime: java.time.LocalDateTime,
        val expenseDetails: ExpenseDetails
    ) : HistoryMovementItem()
}

/**
 * Group of movements by date label
 */
data class MovementGroup(
    val label: String,  // e.g., "HOY", "AYER", "15 Marzo 2025"
    val movements: List<HistoryMovementItem>
)

data class HistoryState(
    val incomeDetails: List<IncomeDetails> = emptyList(),
    val expenseDetails: List<ExpenseDetails> = emptyList(),
    val groupedMovements: List<MovementGroup> = emptyList(),
    val selectedFilter: DateFilter = DateFilter.TODAY,
    val customStartDate: LocalDate? = null,
    val customEndDate: LocalDate? = null,
    val isLoading: Boolean = false,
    val error: AppError? = null,
    val selectedMovement: HistoryMovementItem? = null
)

/**
 * Helper functions to convert from detail models to HistoryMovementItem
 */
fun IncomeDetails.toHistoryMovementItem(): HistoryMovementItem.IncomeItem {
    return HistoryMovementItem.IncomeItem(
        id = income.id,
        datetime = movement.datetime,
        incomeDetails = this
    )
}

fun ExpenseDetails.toHistoryMovementItem(): HistoryMovementItem.ExpenseItem {
    return HistoryMovementItem.ExpenseItem(
        id = expense.id,
        datetime = movement.datetime,
        expenseDetails = this
    )
}
