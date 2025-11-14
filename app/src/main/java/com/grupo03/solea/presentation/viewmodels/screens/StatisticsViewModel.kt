package com.grupo03.solea.presentation.viewmodels.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo03.solea.data.models.Movement
import com.grupo03.solea.data.models.MovementType
import com.grupo03.solea.data.repositories.interfaces.MovementRepository
import com.grupo03.solea.presentation.states.screens.*
import com.grupo03.solea.utils.MovementError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * ViewModel for the Statistics screen.
 *
 * Processes movement data to generate time-series charts,
 * category breakdowns, and summary statistics.
 */
class StatisticsViewModel(
    private val movementRepository: MovementRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StatisticsState())
    val state: StateFlow<StatisticsState> = _state.asStateFlow()

    /**
     * Loads and processes statistics from movements already loaded.
     * This is more efficient when movements are already available (e.g., from HomeScreen).
     */
    fun loadStatisticsFromMovements(movements: List<Movement>, userId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                val balanceOverTime = calculateBalanceOverTime(movements)
                val categoryBreakdown = calculateCategoryBreakdown(movements)
                val monthlyComparison = calculateMonthlyComparisonFromMovements(movements)

                val totalIncome = movements.filter { it.type == MovementType.INCOME }.sumOf { it.total }
                val totalExpenses = movements.filter { it.type == MovementType.EXPENSE }.sumOf { it.total }
                val totalSavings = movements.filter { it.type == MovementType.SAVING }.sumOf { it.total }
                val currentBalance = totalIncome - totalExpenses - totalSavings

                val daysBetween = if (movements.isNotEmpty()) {
                    val minDate = movements.minByOrNull { it.datetime }?.datetime ?: LocalDateTime.now()
                    val maxDate = movements.maxByOrNull { it.datetime }?.datetime ?: LocalDateTime.now()
                    ChronoUnit.DAYS.between(minDate, maxDate).toInt().coerceAtLeast(1)
                } else 1
                val averageDailyExpense = totalExpenses / daysBetween

                val topCategory = categoryBreakdown.maxByOrNull { it.amount }

                _state.value = _state.value.copy(
                    balanceOverTime = balanceOverTime,
                    categoryBreakdown = categoryBreakdown,
                    monthlyComparison = monthlyComparison,
                    totalIncome = totalIncome,
                    totalExpenses = totalExpenses,
                    totalSavings = totalSavings,
                    currentBalance = currentBalance,
                    averageDailyExpense = averageDailyExpense,
                    transactionCount = movements.size,
                    topCategory = topCategory?.category,
                    topCategoryAmount = topCategory?.amount ?: 0.0,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = MovementError.UNKNOWN_ERROR
                )
            }
        }
    }

    /**
     * Loads and processes statistics for a user from repository.
     */
    fun loadStatistics(userId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {

                val movementsResult = movementRepository.getMovementsByUserId(userId)

                if (movementsResult.isError) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = movementsResult.errorOrNull()
                    )
                    return@launch
                }

                val movements = movementsResult.getOrNull() ?: emptyList()

                val balanceOverTime = calculateBalanceOverTime(movements)
                val incomeOverTime = calculateIncomeOverTime(movements)
                val expensesOverTime = calculateExpensesOverTime(movements)
                val categoryBreakdown = calculateCategoryBreakdown(movements)
                val monthlyComparison = calculateMonthlyComparisonFromMovements(movements)

                val totalIncome = movements.filter { it.type == MovementType.INCOME }.sumOf { it.total }
                val totalExpenses = movements.filter { it.type == MovementType.EXPENSE }.sumOf { it.total }
                val totalSavings = movements.filter { it.type == MovementType.SAVING }.sumOf { it.total }
                val currentBalance = totalIncome - totalExpenses - totalSavings

                val daysBetween = if (movements.isNotEmpty()) {
                    val minDate = movements.minByOrNull { it.datetime }?.datetime ?: LocalDateTime.now()
                    val maxDate = movements.maxByOrNull { it.datetime }?.datetime ?: LocalDateTime.now()
                    ChronoUnit.DAYS.between(minDate, maxDate).toInt().coerceAtLeast(1)
                } else 1
                val averageDailyExpense = totalExpenses / daysBetween

                val topCategory = categoryBreakdown.maxByOrNull { it.amount }

                _state.value = _state.value.copy(
                    balanceOverTime = balanceOverTime,
                    incomeOverTime = incomeOverTime,
                    expensesOverTime = expensesOverTime,
                    categoryBreakdown = categoryBreakdown,
                    monthlyComparison = monthlyComparison,
                    totalIncome = totalIncome,
                    totalExpenses = totalExpenses,
                    totalSavings = totalSavings,
                    currentBalance = currentBalance,
                    averageDailyExpense = averageDailyExpense,
                    transactionCount = movements.size,
                    topCategory = topCategory?.category,
                    topCategoryAmount = topCategory?.amount ?: 0.0,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = MovementError.UNKNOWN_ERROR
                )
            }
        }
    }

    /**
     * Calculates cumulative balance over time.
     */
    private fun calculateBalanceOverTime(movements: List<Movement>): List<TimeSeriesDataPoint> {
        val sortedMovements = movements.sortedBy { it.datetime }
        var cumulativeBalance = 0.0

        return sortedMovements.map { movement ->
            when (movement.type) {
                MovementType.INCOME -> cumulativeBalance += movement.total
                MovementType.EXPENSE -> cumulativeBalance -= movement.total
                MovementType.SAVING -> cumulativeBalance -= movement.total
            }
            TimeSeriesDataPoint(movement.datetime, cumulativeBalance)
        }
    }

    /**
     * Calculates income over time.
     */
    private fun calculateIncomeOverTime(movements: List<Movement>): List<TimeSeriesDataPoint> {
        return movements
            .filter { it.type == MovementType.INCOME }
            .sortedBy { it.datetime }
            .map { TimeSeriesDataPoint(it.datetime, it.total) }
    }

    /**
     * Calculates expenses over time.
     */
    private fun calculateExpensesOverTime(movements: List<Movement>): List<TimeSeriesDataPoint> {
        return movements
            .filter { it.type == MovementType.EXPENSE }
            .sortedBy { it.datetime }
            .map { TimeSeriesDataPoint(it.datetime, it.total) }
    }

    /**
     * Calculates category breakdown with percentages.
     */
    private fun calculateCategoryBreakdown(movements: List<Movement>): List<CategoryBreakdown> {
        val expenses = movements.filter { it.type == MovementType.EXPENSE && it.category != null }
        val totalExpenses = expenses.sumOf { it.total }

        if (totalExpenses == 0.0) return emptyList()

        return expenses
            .groupBy { it.category!! }
            .map { (category, categoryMovements) ->
                val amount = categoryMovements.sumOf { it.total }
                val percentage = (amount / totalExpenses) * 100.0
                CategoryBreakdown(
                    category = category,
                    amount = amount,
                    percentage = percentage,
                    transactionCount = categoryMovements.size
                )
            }
            .sortedByDescending { it.amount }
    }

    /**
     * Calculates monthly comparison from movements (not from repository).
     */
    private fun calculateMonthlyComparisonFromMovements(movements: List<Movement>): List<MonthlyData> {
        val monthlyData = mutableListOf<MonthlyData>()
        val formatter = DateTimeFormatter.ofPattern("MMM")
        val now = LocalDateTime.now()

        for (i in 2 downTo 0) {
            val date = now.minusMonths(i.toLong())
            val monthStart = date.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0)
            val monthEnd = date.withDayOfMonth(date.month.length(date.toLocalDate().isLeapYear))
                .withHour(23).withMinute(59).withSecond(59)

            val monthMovements = movements.filter {
                (it.datetime.isAfter(monthStart) || it.datetime.isEqual(monthStart)) &&
                (it.datetime.isBefore(monthEnd) || it.datetime.isEqual(monthEnd))
            }

            val income = monthMovements.filter { it.type == MovementType.INCOME }.sumOf { it.total }
            val expenses = monthMovements.filter { it.type == MovementType.EXPENSE }.sumOf { it.total }
            val savings = monthMovements.filter { it.type == MovementType.SAVING }.sumOf { it.total }

            monthlyData.add(
                MonthlyData(
                    month = date.format(formatter),
                    income = income,
                    expenses = expenses,
                    savings = savings
                )
            )
        }

        return monthlyData
    }
}
