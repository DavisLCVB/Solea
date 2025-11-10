package com.grupo03.solea.presentation.viewmodels.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo03.solea.data.repositories.interfaces.MovementRepository
import com.grupo03.solea.presentation.states.screens.DateFilter
import com.grupo03.solea.presentation.states.screens.HistoryMovementItem
import com.grupo03.solea.presentation.states.screens.HistoryState
import com.grupo03.solea.presentation.states.screens.MovementGroup
import com.grupo03.solea.presentation.states.screens.toHistoryMovementItem
import com.grupo03.solea.data.models.SaveDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * ViewModel for the movement history screen.
 *
 * Manages the display and filtering of financial movement history. Supports multiple
 * date filter options (TODAY, WEEK, MONTH, YEAR, CUSTOM) and groups movements by
 * date labels (HOY, AYER, or specific dates) for better organization.
 *
 * @property movementRepository Repository for fetching movements
 */
class HistoryViewModel(
    private val movementRepository: MovementRepository
) : ViewModel() {

    /** History state including filtered movements, date filter, and grouping */
    private val _historyState = MutableStateFlow(HistoryState())
    val historyState = _historyState.asStateFlow()

    /**
     * Fetches and filters movements for the current date filter.
     *
     * Retrieves all incomes and expenses, filters them by the selected date range,
     * and groups them by date for display.
     *
     * @param userId The ID of the user whose movements to fetch
     */
    fun fetchMovements(userId: String) {
        viewModelScope.launch {
            _historyState.value = _historyState.value.copy(isLoading = true)

            // Calculate date range based on selected filter
            val (startDate, endDate) = calculateDateRange(_historyState.value.selectedFilter)

            // Fetch incomes, expenses, and savings
            val incomesResult = movementRepository.getIncomesByUserId(userId)
            val expensesResult = movementRepository.getExpensesByUserId(userId)
            val savingsResult = movementRepository.getSavingsByUserId(userId)

            if (!incomesResult.isSuccess) {
                _historyState.value = _historyState.value.copy(
                    isLoading = false,
                    error = incomesResult.errorOrNull()
                )
                return@launch
            }
            if (!expensesResult.isSuccess) {
                _historyState.value = _historyState.value.copy(
                    isLoading = false,
                    error = expensesResult.errorOrNull()
                )
                return@launch
            }
            if (!savingsResult.isSuccess) {
                _historyState.value = _historyState.value.copy(
                    isLoading = false,
                    error = savingsResult.errorOrNull()
                )
                return@launch
            }

            val incomes = incomesResult.getOrNull() ?: emptyList()
            val expenses = expensesResult.getOrNull() ?: emptyList()
            val savings = savingsResult.getOrNull() ?: emptyList()

            // Convert to HistoryMovementItem and filter by date range
            val allMovements = (
                    incomes.map { it.toHistoryMovementItem() } +
                            expenses.map { it.toHistoryMovementItem() } +
                            savings.map { it.toHistoryMovementItem() }
                    )
                .filter { it.datetime >= startDate && it.datetime <= endDate }
                .sortedByDescending { it.datetime }

            // Group movements by date
            val groupedMovements = groupMovementsByDate(allMovements)

            _historyState.value = _historyState.value.copy(
                incomeDetails = incomes,
                expenseDetails = expenses,
                saveDetails = savings,
                groupedMovements = groupedMovements,
                isLoading = false,
                error = null
            )
        }
    }

    /**
     * Changes the active date filter and re-filters movements.
     *
     * Re-applies the new filter to the currently loaded movements without fetching again.
     *
     * @param filter The new date filter to apply
     */
    fun onFilterSelected(filter: DateFilter) {
        _historyState.value = _historyState.value.copy(selectedFilter = filter)
        // Re-apply filtering to current movements
        val (startDate, endDate) = calculateDateRange(filter)

        val allMovements = (
            _historyState.value.incomeDetails.map { it.toHistoryMovementItem() } +
            _historyState.value.expenseDetails.map { it.toHistoryMovementItem() } +
            _historyState.value.saveDetails.map { it.toHistoryMovementItem() }
        )
            .filter { it.datetime >= startDate && it.datetime <= endDate }
            .sortedByDescending { it.datetime }

        val groupedMovements = groupMovementsByDate(allMovements)

        _historyState.value = _historyState.value.copy(
            groupedMovements = groupedMovements
        )
    }

    /**
     * Sets a custom date range filter.
     *
     * Updates the filter to CUSTOM and applies the specified date range to filter movements.
     *
     * @param startDate Start date of the custom range (inclusive)
     * @param endDate End date of the custom range (inclusive)
     */
    fun onCustomDateRangeSelected(startDate: LocalDate, endDate: LocalDate) {
        _historyState.value = _historyState.value.copy(
            selectedFilter = DateFilter.CUSTOM,
            customStartDate = startDate,
            customEndDate = endDate
        )

        val startDateTime = startDate.atStartOfDay()
        val endDateTime = endDate.atTime(LocalTime.MAX)

        val allMovements = (
            _historyState.value.incomeDetails.map { it.toHistoryMovementItem() } +
            _historyState.value.expenseDetails.map { it.toHistoryMovementItem() }
        )
            .filter { it.datetime >= startDateTime && it.datetime <= endDateTime }
            .sortedByDescending { it.datetime }

        val groupedMovements = groupMovementsByDate(allMovements)

        _historyState.value = _historyState.value.copy(
            groupedMovements = groupedMovements
        )
    }

    /**
     * Sets the selected movement for detail viewing.
     *
     * @param movement The movement to select, or null to clear selection
     */
    fun onMovementSelected(movement: HistoryMovementItem?) {
        _historyState.value = _historyState.value.copy(selectedMovement = movement)
    }

    /**
     * Calculates the date range for a given filter.
     *
     * @param filter The date filter to calculate range for
     * @return Pair of start and end LocalDateTime for the filter
     */
    private fun calculateDateRange(filter: DateFilter): Pair<LocalDateTime, LocalDateTime> {
        val now = LocalDateTime.now()
        val today = LocalDate.now()

        return when (filter) {
            DateFilter.TODAY -> {
                val startOfDay = today.atStartOfDay()
                val endOfDay = today.atTime(LocalTime.MAX)
                startOfDay to endOfDay
            }

            DateFilter.WEEK -> {
                val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1).atStartOfDay()
                val endOfWeek = today.atTime(LocalTime.MAX)
                startOfWeek to endOfWeek
            }

            DateFilter.MONTH -> {
                val startOfMonth = today.withDayOfMonth(1).atStartOfDay()
                val endOfMonth = today.atTime(LocalTime.MAX)
                startOfMonth to endOfMonth
            }

            DateFilter.YEAR -> {
                val startOfYear = today.withDayOfYear(1).atStartOfDay()
                val endOfYear = today.atTime(LocalTime.MAX)
                startOfYear to endOfYear
            }

            DateFilter.CUSTOM -> {
                val start = (_historyState.value.customStartDate ?: today).atStartOfDay()
                val end = (_historyState.value.customEndDate ?: today).atTime(LocalTime.MAX)
                start to end
            }
        }
    }

    /**
     * Groups movements by date with localized labels.
     *
     * Creates MovementGroup objects with labels like "HOY" (today), "AYER" (yesterday),
     * or formatted dates (e.g., "15 Marzo 2025"). Movements within each group are sorted
     * by datetime descending.
     *
     * @param movements List of movements to group
     * @return List of MovementGroup sorted by date descending
     */
    private fun groupMovementsByDate(movements: List<HistoryMovementItem>): List<MovementGroup> {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        return movements
            .groupBy { it.datetime.toLocalDate() }
            .map { (date, movementsOnDate) ->
                val label = when (date) {
                    today -> "HOY"
                    yesterday -> "AYER"
                    else -> {
                        // Format as "15 Marzo 2025"
                        val formatter = DateTimeFormatter.ofPattern(
                            "d MMMM yyyy",
                            Locale.getDefault()
                        )
                        date.format(formatter)
                    }
                }
                MovementGroup(label, movementsOnDate.sortedByDescending { it.datetime })
            }
            .sortedByDescending { group ->
                // Sort groups by the first movement's date
                group.movements.firstOrNull()?.datetime ?: LocalDateTime.MIN
            }
    }

    /**
     * Gets a formatted text representation of the current date range.
     *
     * @return Formatted date range string (e.g., "15 Marzo 2025" for TODAY, or "1 Marzo 2025 - 31 Marzo 2025" for MONTH)
     */
    fun getDateRangeText(): String {
        val (startDate, endDate) = calculateDateRange(_historyState.value.selectedFilter)
        val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault())

        return if (_historyState.value.selectedFilter == DateFilter.TODAY) {
            LocalDate.now().format(formatter)
        } else {
            "${startDate.toLocalDate().format(formatter)} - ${
                endDate.toLocalDate().format(formatter)
            }"
        }
    }

    /**
     * Updates movements from external sources (e.g., real-time observers).
     *
     * Filters and groups the provided movements according to the current date filter.
     * Useful for updating the history when observing movements in real-time.
     *
     * @param incomeDetails List of income details
     * @param expenseDetails List of expense details
     */
    fun updateMovements(
        incomeDetails: List<com.grupo03.solea.data.models.IncomeDetails>,
        expenseDetails: List<com.grupo03.solea.data.models.ExpenseDetails>,
        saveDetails: List<SaveDetails> = emptyList()
    ) {
        val (startDate, endDate) = calculateDateRange(_historyState.value.selectedFilter)

        val allMovements = (
            incomeDetails.map { it.toHistoryMovementItem() } +
            expenseDetails.map { it.toHistoryMovementItem() } +
            saveDetails.map { it.toHistoryMovementItem() }
        )
            .filter { it.datetime >= startDate && it.datetime <= endDate }
            .sortedByDescending { it.datetime }

        val groupedMovements = groupMovementsByDate(allMovements)

        _historyState.value = _historyState.value.copy(
            incomeDetails = incomeDetails,
            expenseDetails = expenseDetails,
            saveDetails = saveDetails,
            groupedMovements = groupedMovements,
            isLoading = false,
            error = null
        )
    }
}
