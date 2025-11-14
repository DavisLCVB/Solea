package com.grupo03.solea.presentation.states.screens

import com.grupo03.solea.data.models.Movement
import com.grupo03.solea.utils.AppError
import java.time.LocalDateTime

/**
 * Time period filter for statistics.
 */
enum class TimePeriod {
    WEEK,
    MONTH,
    THREE_MONTHS,
    YEAR
}

/**
 * Data point for time-series charts.
 */
data class TimeSeriesDataPoint(
    val date: LocalDateTime,
    val value: Double
)

/**
 * Category breakdown for expense distribution.
 */
data class CategoryBreakdown(
    val category: String,
    val amount: Double,
    val percentage: Double,
    val transactionCount: Int
)

/**
 * Monthly comparison data.
 */
data class MonthlyData(
    val month: String, // "Jan 2024", "Feb 2024", etc.
    val income: Double,
    val expenses: Double,
    val savings: Double
)

/**
 * UI state for the Statistics screen.
 *
 * Contains processed data ready for visualization.
 */
data class StatisticsState(
    // Original data
    val allMovements: List<Movement> = emptyList(),

    // Filters
    val selectedPeriod: TimePeriod = TimePeriod.MONTH,

    // Time-series data
    val balanceOverTime: List<TimeSeriesDataPoint> = emptyList(),
    val incomeOverTime: List<TimeSeriesDataPoint> = emptyList(),
    val expensesOverTime: List<TimeSeriesDataPoint> = emptyList(),

    // Category breakdown
    val categoryBreakdown: List<CategoryBreakdown> = emptyList(),

    // Monthly comparison (last 6 months)
    val monthlyComparison: List<MonthlyData> = emptyList(),

    // Summary statistics
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val totalSavings: Double = 0.0,
    val currentBalance: Double = 0.0,
    val averageDailyExpense: Double = 0.0,
    val transactionCount: Int = 0,
    val topCategory: String? = null,
    val topCategoryAmount: Double = 0.0,

    // State
    val isLoading: Boolean = false,
    val error: AppError? = null
)
