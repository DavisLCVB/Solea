package com.grupo03.solea.ui.screens.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.grupo03.solea.R
import com.grupo03.solea.presentation.viewmodels.screens.StatisticsViewModel
import com.grupo03.solea.presentation.viewmodels.shared.AuthViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    statisticsViewModel: StatisticsViewModel,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state = statisticsViewModel.state.collectAsState()
    val authState = authViewModel.authState.collectAsState()
    val userId = authState.value.user?.uid ?: return

    LaunchedEffect(userId, state.value.transactionCount) {
        if (state.value.transactionCount == 0 && !state.value.isLoading) {
            statisticsViewModel.loadStatistics(userId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.statistics_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { paddingValues ->
        if (state.value.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SummaryCards(state.value)

                if (state.value.balanceOverTime.isNotEmpty()) {
                    BalanceOverTimeChart(state.value)
                }

                if (state.value.categoryBreakdown.isNotEmpty()) {
                    CategoryBreakdownCard(state.value)
                }

                if (state.value.categoryBreakdown.isNotEmpty()) {
                    CategoryPieChart(state.value)
                }

                if (state.value.monthlyComparison.isNotEmpty()) {
                    MonthlyComparisonChart(state.value)
                }
            }
        }
    }
}

@Composable
private fun SummaryCards(state: com.grupo03.solea.presentation.states.screens.StatisticsState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryCard(
            title = stringResource(R.string.balance),
            value = "$%.2f".format(state.currentBalance),
            modifier = Modifier.weight(1f),
            valueColor = if (state.currentBalance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
        )
        SummaryCard(
            title = stringResource(R.string.transactions),
            value = state.transactionCount.toString(),
            modifier = Modifier.weight(1f)
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryCard(
            title = stringResource(R.string.avg_daily_expense),
            value = "$%.2f".format(state.averageDailyExpense),
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            title = stringResource(R.string.savings_label),
            value = "$%.2f".format(state.totalSavings),
            modifier = Modifier.weight(1f),
            valueColor = Color(0xFFFFC107)
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryCard(
            title = stringResource(R.string.top_category),
            value = state.topCategory ?: stringResource(R.string.none),
            subtitle = if (state.topCategoryAmount > 0) "$%.2f".format(state.topCategoryAmount) else null,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BalanceOverTimeChart(state: com.grupo03.solea.presentation.states.screens.StatisticsState) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.balance_over_time),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            val modelProducer = remember { CartesianChartModelProducer() }
            val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM") }

            LaunchedEffect(state.balanceOverTime) {
                modelProducer.runTransaction {
                    lineSeries {
                        series(state.balanceOverTime.map { it.value })
                    }
                }
            }

            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberLineCartesianLayer(),
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(
                        valueFormatter = { value, _, _ ->
                            val index = value.toInt()
                            if (index >= 0 && index < state.balanceOverTime.size) {
                                state.balanceOverTime[index].date.format(dateFormatter)
                            } else {
                                ""
                            }
                        }
                    )
                ),
                modelProducer = modelProducer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

@Composable
private fun CategoryBreakdownCard(state: com.grupo03.solea.presentation.states.screens.StatisticsState) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.expenses_by_category),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            state.categoryBreakdown.take(5).forEach { category ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = category.category,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "$%.2f (%.1f%%)".format(category.amount, category.percentage),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    LinearProgressIndicator(
                        progress = { (category.percentage / 100.0).toFloat() },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryPieChart(state: com.grupo03.solea.presentation.states.screens.StatisticsState) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.expense_distribution),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            val categories = state.categoryBreakdown.take(4)
            val savingsLabel = stringResource(R.string.savings_label)

            val totalExpenses = categories.sumOf { it.amount }
            val totalWithSavings = totalExpenses + state.totalSavings

            data class PieChartItem(val label: String, val amount: Double, val percentage: Double)
            val pieItems = buildList {
                addAll(categories.map {
                    PieChartItem(
                        label = it.category,
                        amount = it.amount,
                        percentage = if (totalWithSavings > 0) (it.amount / totalWithSavings * 100) else 0.0
                    )
                })
                if (state.totalSavings > 0) {
                    add(PieChartItem(
                        label = savingsLabel,
                        amount = state.totalSavings,
                        percentage = if (totalWithSavings > 0) (state.totalSavings / totalWithSavings * 100) else 0.0
                    ))
                }
            }

            val colors = listOf(
                Color(0xFF2196F3),
                Color(0xFFFF5722),
                Color(0xFF9C27B0),
                Color(0xFF4CAF50),
                Color(0xFFFFC107)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier.size(120.dp)
                    ) {
                        var startAngle = -90f

                        pieItems.forEachIndexed { index, item ->
                            val sweepAngle = (item.percentage / 100 * 360).toFloat()
                            drawArc(
                                color = colors[index % colors.size],
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = true
                            )
                            startAngle += sweepAngle
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pieItems.forEachIndexed { index, item ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(colors[index % colors.size])
                            )
                            Column {
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "%.1f%%".format(item.percentage),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyComparisonChart(state: com.grupo03.solea.presentation.states.screens.StatisticsState) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.monthly_comparison),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            state.monthlyComparison.forEach { monthData ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = monthData.month,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    val maxValue = state.monthlyComparison.maxOfOrNull {
                        maxOf(it.income, it.expenses, it.savings)
                    } ?: 1.0

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color(0xFF4CAF50))
                        )
                        val incomeWidth = if (maxValue > 0) ((monthData.income / maxValue) * 100).toFloat() else 0f
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(20.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(incomeWidth / 100f)
                                    .background(Color(0xFF4CAF50))
                            )
                        }
                        Text(
                            text = "$%.0f".format(monthData.income),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.width(60.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color(0xFFF44336))
                        )
                        val expenseWidth = if (maxValue > 0) ((monthData.expenses / maxValue) * 100).toFloat() else 0f
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(20.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(expenseWidth / 100f)
                                    .background(Color(0xFFF44336))
                            )
                        }
                        Text(
                            text = "$%.0f".format(monthData.expenses),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.width(60.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color(0xFFFFC107))
                        )
                        val savingsWidth = if (maxValue > 0) ((monthData.savings / maxValue) * 100).toFloat() else 0f
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(20.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(savingsWidth / 100f)
                                    .background(Color(0xFFFFC107))
                            )
                        }
                        Text(
                            text = "$%.0f".format(monthData.savings),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.width(60.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                LegendItem(
                    color = Color(0xFF4CAF50),
                    label = stringResource(R.string.income)
                )
                LegendItem(
                    color = Color(0xFFF44336),
                    label = stringResource(R.string.expenses)
                )
                LegendItem(
                    color = Color(0xFFFFC107),
                    label = stringResource(R.string.savings_label)
                )
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color = color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
