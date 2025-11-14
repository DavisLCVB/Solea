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
            _state.value = _state.value.copy(isLoading = true, error = null, allMovements = movements)

            try {
                // Filter movements by selected period for some charts
                val filteredMovements = filterMovementsByPeriod(movements, _state.value.selectedPeriod)

                // Balance over time uses ALL movements (no filter)
                val balanceOverTime = calculateBalanceOverTime(movements)

                // Category breakdown uses filtered movements
                val categoryBreakdown = calculateCategoryBreakdown(filteredMovements)

                // Monthly comparison uses filtered movements
                val monthlyComparison = calculateMonthlyComparisonFromMovements(filteredMovements)

                // Calculate summary stats from filtered movements
                val totalIncome = filteredMovements.filter { it.type == MovementType.INCOME }.sumOf { it.total }
                val totalExpenses = filteredMovements.filter { it.type == MovementType.EXPENSE }.sumOf { it.total }
                val totalSavings = filteredMovements.filter { it.type == MovementType.SAVING }.sumOf { it.total }
                val currentBalance = totalIncome - totalExpenses - totalSavings

                // Calculate average daily expense based on filtered movements
                val daysBetween = if (filteredMovements.isNotEmpty()) {
                    val minDate = filteredMovements.minByOrNull { it.datetime }?.datetime ?: LocalDateTime.now()
                    val maxDate = filteredMovements.maxByOrNull { it.datetime }?.datetime ?: LocalDateTime.now()
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
                    transactionCount = filteredMovements.size,
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
                // Get date range based on selected period
                val (startDate, endDate) = getDateRange(_state.value.selectedPeriod)

                // Fetch movements in range
                val movementsResult = movementRepository.getMovementsByUserAndDateRange(
                    userId, startDate, endDate
                )

                if (movementsResult.isError) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = movementsResult.errorOrNull()
                    )
                    return@launch
                }

                val movements = movementsResult.getOrNull() ?: emptyList()

                // Process data
                val balanceOverTime = calculateBalanceOverTime(movements)
                val incomeOverTime = calculateIncomeOverTime(movements)
                val expensesOverTime = calculateExpensesOverTime(movements)
                val categoryBreakdown = calculateCategoryBreakdown(movements)
                val monthlyComparison = calculateMonthlyComparison(userId)

                // Calculate summary stats
                val totalIncome = movements.filter { it.type == MovementType.INCOME }.sumOf { it.total }
                val totalExpenses = movements.filter { it.type == MovementType.EXPENSE }.sumOf { it.total }
                val totalSavings = movements.filter { it.type == MovementType.SAVING }.sumOf { it.total }
                val currentBalance = totalIncome - totalExpenses - totalSavings

                val daysBetween = ChronoUnit.DAYS.between(startDate, endDate).toInt().coerceAtLeast(1)
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
     * Changes the time period filter and recalculates filtered statistics.
     */
    fun changePeriod(period: TimePeriod, userId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(selectedPeriod = period, isLoading = true)

            try {
                val allMovements = _state.value.allMovements
                if (allMovements.isEmpty()) {
                    loadStatistics(userId)
                    return@launch
                }

                // Filter movements by new period
                val filteredMovements = filterMovementsByPeriod(allMovements, period)

                // Recalculate only filtered statistics
                val categoryBreakdown = calculateCategoryBreakdown(filteredMovements)
                val monthlyComparison = calculateMonthlyComparisonFromMovements(filteredMovements)

                // Calculate summary stats
                val totalIncome = filteredMovements.filter { it.type == MovementType.INCOME }.sumOf { it.total }
                val totalExpenses = filteredMovements.filter { it.type == MovementType.EXPENSE }.sumOf { it.total }
                val totalSavings = filteredMovements.filter { it.type == MovementType.SAVING }.sumOf { it.total }
                val currentBalance = totalIncome - totalExpenses - totalSavings

                val daysBetween = if (filteredMovements.isNotEmpty()) {
                    val minDate = filteredMovements.minByOrNull { it.datetime }?.datetime ?: LocalDateTime.now()
                    val maxDate = filteredMovements.maxByOrNull { it.datetime }?.datetime ?: LocalDateTime.now()
                    ChronoUnit.DAYS.between(minDate, maxDate).toInt().coerceAtLeast(1)
                } else 1
                val averageDailyExpense = totalExpenses / daysBetween

                val topCategory = categoryBreakdown.maxByOrNull { it.amount }

                _state.value = _state.value.copy(
                    categoryBreakdown = categoryBreakdown,
                    monthlyComparison = monthlyComparison,
                    totalIncome = totalIncome,
                    totalExpenses = totalExpenses,
                    totalSavings = totalSavings,
                    currentBalance = currentBalance,
                    averageDailyExpense = averageDailyExpense,
                    transactionCount = filteredMovements.size,
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
     * Gets start and end dates for a period.
     */
    private fun getDateRange(period: TimePeriod): Pair<LocalDateTime, LocalDateTime> {
        val now = LocalDateTime.now()
        val startDate = when (period) {
            TimePeriod.WEEK -> now.minusWeeks(1)
            TimePeriod.MONTH -> now.minusMonths(1)
            TimePeriod.THREE_MONTHS -> now.minusMonths(3)
            TimePeriod.YEAR -> now.minusYears(1)
        }
        return Pair(startDate, now)
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
     * Calculates monthly comparison for the last 3 months.
     */
    private suspend fun calculateMonthlyComparison(userId: String): List<MonthlyData> {
        val monthlyData = mutableListOf<MonthlyData>()
        val formatter = DateTimeFormatter.ofPattern("MMM")
        val now = LocalDateTime.now()

        for (i in 2 downTo 0) {
            val date = now.minusMonths(i.toLong())
            val year = date.year
            val month = date.monthValue

            val expensesResult = movementRepository.getMonthlyExpensesByUser(userId, year, month)
            val incomesResult = movementRepository.getMonthlyIncomesByUser(userId, year, month)

            val expenses = expensesResult.getOrNull() ?: 0.0
            val incomes = incomesResult.getOrNull() ?: 0.0

            // Get savings for the month
            val movementsResult = movementRepository.getMovementsByUserAndDateRange(
                userId,
                date.withDayOfMonth(1).withHour(0).withMinute(0),
                date.withDayOfMonth(date.month.length(date.toLocalDate().isLeapYear)).withHour(23).withMinute(59)
            )
            val savings = movementsResult.getOrNull()
                ?.filter { it.type == MovementType.SAVING }
                ?.sumOf { it.total } ?: 0.0

            monthlyData.add(
                MonthlyData(
                    month = date.format(formatter),
                    income = incomes,
                    expenses = expenses,
                    savings = savings
                )
            )
        }

        return monthlyData
    }

    /**
     * Filters movements by time period.
     */
    private fun filterMovementsByPeriod(movements: List<Movement>, period: TimePeriod): List<Movement> {
        val now = LocalDateTime.now()
        val startDate = when (period) {
            TimePeriod.WEEK -> now.minusWeeks(1)
            TimePeriod.MONTH -> now.minusMonths(1)
            TimePeriod.THREE_MONTHS -> now.minusMonths(3)
            TimePeriod.YEAR -> now.minusYears(1)
        }

        return movements.filter { it.datetime.isAfter(startDate) || it.datetime.isEqual(startDate) }
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
