package com.grupo03.solea.presentation.viewmodels.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo03.solea.data.repositories.interfaces.MovementRepository
import com.grupo03.solea.presentation.states.screens.DateFilter
import com.grupo03.solea.presentation.states.screens.HistoryMovementItem
import com.grupo03.solea.presentation.states.screens.HistoryState
import com.grupo03.solea.presentation.states.screens.MovementGroup
import com.grupo03.solea.presentation.states.screens.toHistoryMovementItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class HistoryViewModel(
    private val movementRepository: MovementRepository
) : ViewModel() {

    private val _historyState = MutableStateFlow(HistoryState())
    val historyState = _historyState.asStateFlow()

    fun fetchMovements(userId: String) {
        viewModelScope.launch {
            _historyState.value = _historyState.value.copy(isLoading = true)

            // Calculate date range based on selected filter
            val (startDate, endDate) = calculateDateRange(_historyState.value.selectedFilter)

            // Fetch incomes and expenses
            val incomesResult = movementRepository.getIncomesByUserId(userId)
            val expensesResult = movementRepository.getExpensesByUserId(userId)

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

            val incomes = incomesResult.getOrNull() ?: emptyList()
            val expenses = expensesResult.getOrNull() ?: emptyList()

            // Convert to HistoryMovementItem and filter by date range
            val allMovements = (
                    incomes.map { it.toHistoryMovementItem() } +
                            expenses.map { it.toHistoryMovementItem() }
                    )
                .filter { it.datetime >= startDate && it.datetime <= endDate }
                .sortedByDescending { it.datetime }

            // Group movements by date
            val groupedMovements = groupMovementsByDate(allMovements)

            _historyState.value = _historyState.value.copy(
                incomeDetails = incomes,
                expenseDetails = expenses,
                groupedMovements = groupedMovements,
                isLoading = false,
                error = null
            )
        }
    }

    fun onFilterSelected(filter: DateFilter) {
        _historyState.value = _historyState.value.copy(selectedFilter = filter)
        // Re-apply filtering to current movements
        val (startDate, endDate) = calculateDateRange(filter)

        val allMovements = (
            _historyState.value.incomeDetails.map { it.toHistoryMovementItem() } +
            _historyState.value.expenseDetails.map { it.toHistoryMovementItem() }
        )
            .filter { it.datetime >= startDate && it.datetime <= endDate }
            .sortedByDescending { it.datetime }

        val groupedMovements = groupMovementsByDate(allMovements)

        _historyState.value = _historyState.value.copy(
            groupedMovements = groupedMovements
        )
    }

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

    fun onMovementSelected(movement: HistoryMovementItem?) {
        _historyState.value = _historyState.value.copy(selectedMovement = movement)
    }

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

    fun updateMovements(
        incomeDetails: List<com.grupo03.solea.data.models.IncomeDetails>,
        expenseDetails: List<com.grupo03.solea.data.models.ExpenseDetails>
    ) {
        val (startDate, endDate) = calculateDateRange(_historyState.value.selectedFilter)

        val allMovements = (
            incomeDetails.map { it.toHistoryMovementItem() } +
            expenseDetails.map { it.toHistoryMovementItem() }
        )
            .filter { it.datetime >= startDate && it.datetime <= endDate }
            .sortedByDescending { it.datetime }

        val groupedMovements = groupMovementsByDate(allMovements)

        _historyState.value = _historyState.value.copy(
            incomeDetails = incomeDetails,
            expenseDetails = expenseDetails,
            groupedMovements = groupedMovements,
            isLoading = false,
            error = null
        )
    }
}
