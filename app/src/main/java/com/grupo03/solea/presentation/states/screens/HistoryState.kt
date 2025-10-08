package com.grupo03.solea.presentation.states.screens

import com.grupo03.solea.data.models.ExpenseDetails
import com.grupo03.solea.data.models.IncomeDetails
import com.grupo03.solea.utils.AppError
import java.time.LocalDate

/**
 * Date filter options for viewing movement history.
 */
enum class DateFilter {
    /** Today's movements */
    TODAY,

    /** Current week's movements */
    WEEK,

    /** Current month's movements */
    MONTH,

    /** Current year's movements */
    YEAR,

    /** Custom date range */
    CUSTOM
}

/**
 * Unified movement representation for history display.
 *
 * This sealed class allows treating income and expenses uniformly in lists
 * while preserving their specific data.
 */
sealed class HistoryMovementItem {
    /** Unique identifier of the movement */
    abstract val id: String

    /** Date and time when the movement occurred */
    abstract val datetime: java.time.LocalDateTime

    /**
     * Income movement item.
     *
     * @property id Income ID
     * @property datetime When the income occurred
     * @property incomeDetails Complete income details
     */
    data class IncomeItem(
        override val id: String,
        override val datetime: java.time.LocalDateTime,
        val incomeDetails: IncomeDetails
    ) : HistoryMovementItem()

    /**
     * Expense movement item.
     *
     * @property id Expense ID
     * @property datetime When the expense occurred
     * @property expenseDetails Complete expense details including source
     */
    data class ExpenseItem(
        override val id: String,
        override val datetime: java.time.LocalDateTime,
        val expenseDetails: ExpenseDetails
    ) : HistoryMovementItem()
}

/**
 * Group of movements by date label.
 *
 * Movements are grouped by date labels like "HOY" (today), "AYER" (yesterday),
 * or specific dates for better organization in the history view.
 *
 * @property label Display label for the group (e.g., "HOY", "AYER", "15 Marzo 2025")
 * @property movements List of movements in this group
 */
data class MovementGroup(
    val label: String,
    val movements: List<HistoryMovementItem>
)

/**
 * UI state for the movement history screen.
 *
 * Manages the state of the transaction history view, including filtering,
 * grouping, and selection of movements.
 *
 * @property incomeDetails Raw list of income details
 * @property expenseDetails Raw list of expense details
 * @property groupedMovements Movements organized by date groups for display
 * @property selectedFilter Currently active date filter
 * @property customStartDate Start date for custom filter, null if not using custom filter
 * @property customEndDate End date for custom filter, null if not using custom filter
 * @property isLoading Whether movements are being loaded
 * @property error Error that occurred during loading, null if no error
 * @property selectedMovement Movement selected for detailed view, null if none selected
 */
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
 * Converts IncomeDetails to HistoryMovementItem for unified display.
 *
 * @return IncomeItem containing this income's details
 */
fun IncomeDetails.toHistoryMovementItem(): HistoryMovementItem.IncomeItem {
    return HistoryMovementItem.IncomeItem(
        id = income.id,
        datetime = movement.datetime,
        incomeDetails = this
    )
}

/**
 * Converts ExpenseDetails to HistoryMovementItem for unified display.
 *
 * @return ExpenseItem containing this expense's details
 */
fun ExpenseDetails.toHistoryMovementItem(): HistoryMovementItem.ExpenseItem {
    return HistoryMovementItem.ExpenseItem(
        id = expense.id,
        datetime = movement.datetime,
        expenseDetails = this
    )
}
