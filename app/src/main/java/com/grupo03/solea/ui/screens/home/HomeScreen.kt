package com.grupo03.solea.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.grupo03.solea.R
import com.grupo03.solea.data.models.Category
import com.grupo03.solea.data.models.ExpenseDetails
import com.grupo03.solea.data.models.IncomeDetails
import com.grupo03.solea.data.models.Movement
import com.grupo03.solea.data.models.SaveDetails
import com.grupo03.solea.presentation.states.screens.HistoryMovementItem
import com.grupo03.solea.presentation.states.screens.toHistoryMovementItem
import com.grupo03.solea.presentation.states.shared.MovementsState
import com.grupo03.solea.presentation.viewmodels.screens.HomeViewModel
import com.grupo03.solea.presentation.viewmodels.screens.StatisticsViewModel
import com.grupo03.solea.presentation.viewmodels.shared.AuthViewModel
import com.grupo03.solea.presentation.viewmodels.shared.MovementsViewModel
import com.grupo03.solea.ui.components.ExpandableFab
import com.grupo03.solea.ui.components.FabMenuItem
import com.grupo03.solea.ui.components.MovementCard
import com.grupo03.solea.ui.components.MovementDetailsModal
import com.grupo03.solea.ui.components.SectionTitle
import com.grupo03.solea.ui.components.TopBar
import com.grupo03.solea.ui.theme.SoleaTheme
import com.grupo03.solea.ui.theme.soleaColors
import com.grupo03.solea.utils.CurrencyUtils
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.shape.Shape
import java.util.Locale

sealed class MovementItem {
    abstract val movement: Movement

    data class IncomeItem(
        val incomeDetails: IncomeDetails
    ) : MovementItem() {
        override val movement: Movement = incomeDetails.movement
    }

    data class ExpenseItem(
        val expenseDetails: ExpenseDetails
    ) : MovementItem() {
        override val movement: Movement = expenseDetails.movement
    }

    data class SaveItem(
        val saveDetails: SaveDetails
    ) : MovementItem() {
        override val movement: Movement = saveDetails.movement
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    authViewModel: AuthViewModel,
    movementsViewModel: MovementsViewModel,
    statisticsViewModel: com.grupo03.solea.presentation.viewmodels.screens.StatisticsViewModel,
    onNavigateToNewMovement: () -> Unit,
    onNavigateToNewCategory: () -> Unit,
    onNavigateToScanReceipt: () -> Unit,
    onNavigateToAudioAnalysis: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {}
) {
    val homeState = homeViewModel.homeState.collectAsState()
    val movementsState = movementsViewModel.movementsState.collectAsState()
    val userId = authViewModel.authState.collectAsState().value.user!!.uid

    LaunchedEffect(userId) {
        movementsViewModel.observeMovements(userId)
        movementsViewModel.observeCategories(userId)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopBar(title = stringResource(R.string.home_title))

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            HomeScreenContent(
                movementsState = movementsState.value,
                homeViewModel = homeViewModel,
                homeState = homeState.value,
                movementsViewModel = movementsViewModel,
                userId = userId,
                statisticsViewModel = statisticsViewModel,
                onNavigateToStatistics = onNavigateToStatistics
            )

            ExpandableFab(
                expanded = homeState.value.fabExpanded,
                onToggle = homeViewModel::onToggleFab,
                items = listOf(
                    FabMenuItem(
                        iconRes = R.drawable.form,
                        label = stringResource(R.string.button_new_movement),
                        onClick = {
                            homeViewModel.onCollapseFab()
                            onNavigateToNewMovement()
                        }
                    ),
                    FabMenuItem(
                        icon = Icons.Default.CameraAlt,
                        label = stringResource(R.string.button_scan_receipt),
                        onClick = {
                            homeViewModel.onCollapseFab()
                            onNavigateToScanReceipt()
                        }
                    ),
                    FabMenuItem(
                        icon = Icons.Default.Mic,
                        label = stringResource(R.string.button_voice_note),
                        onClick = {
                            homeViewModel.onCollapseFab()
                            onNavigateToAudioAnalysis()
                        }
                    ),
                    FabMenuItem(
                        icon = Icons.Default.Category,
                        label = stringResource(R.string.button_new_category),
                        onClick = {
                            homeViewModel.onCollapseFab()
                            onNavigateToNewCategory()
                        }
                    )
                )
            )
        }
    }

}


@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    movementsState: MovementsState = MovementsState(),
    homeViewModel: HomeViewModel? = null,
    homeState: com.grupo03.solea.presentation.states.screens.HomeScreenState? = null,
    movementsViewModel: MovementsViewModel? = null,
    userId: String = "",
    statisticsViewModel: StatisticsViewModel? = null,
    onNavigateToStatistics: () -> Unit = {}
) {
    val totalIncome = movementsState.incomeDetailsList.sumOf { it.movement.total }
    val totalExpense = movementsState.expenseDetailsList.sumOf { it.movement.total }
    val totalSavings = movementsState.saveDetailsList.sumOf { it.movement.total }
    val balance = totalIncome - totalExpense - totalSavings

    val currency = (movementsState.incomeDetailsList.firstOrNull()?.movement?.currency
        ?: movementsState.expenseDetailsList.firstOrNull()?.movement?.currency
        ?: CurrencyUtils.getCurrencyByCountry())

    val allMovements = buildList {
        addAll(movementsState.incomeDetailsList.map { MovementItem.IncomeItem(it) })
        addAll(movementsState.expenseDetailsList.map { MovementItem.ExpenseItem(it) })
        addAll(movementsState.saveDetailsList.map { MovementItem.SaveItem(it) })
    }.sortedByDescending { it.movement.datetime }

    val movements = buildList {
        addAll(movementsState.incomeDetailsList.map { it.movement })
        addAll(movementsState.expenseDetailsList.map { it.movement })
        addAll(movementsState.saveDetailsList.map { it.movement })
    }

    LaunchedEffect(movements.size, userId) {
        if (movements.isNotEmpty()) {
            statisticsViewModel?.loadStatisticsFromMovements(movements, userId)
        }
    }

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Balance Card
            BalanceCard(
                currentBalance = balance,
                income = totalIncome,
                outcome = totalExpense,
                currency = currency
            )

            // Income vs Expenses Chart
            ChartCard(
                income = totalIncome,
                expenses = totalExpense,
                currency = currency,
                onClick = onNavigateToStatistics
            )

            // Section title
            SectionTitle(
                text = stringResource(R.string.home_last_movements),
                icon = Icons.Default.History
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 20.dp)
                        .zIndex(10f)
                        .align(Alignment.TopCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.background,
                                    MaterialTheme.colorScheme.background,
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.background.copy(alpha = 0f),
                                )
                            ),
                            shape = RectangleShape
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 40.dp, bottom = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    allMovements.forEach { movementItem ->
                        when (movementItem) {
                            is MovementItem.IncomeItem -> {
                                Card(
                                    onClick = {
                                        homeViewModel?.onMovementSelected(
                                            movementItem.incomeDetails.toHistoryMovementItem()
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                                    )
                                ) {
                                    MovementCard(
                                        incomeDetails = movementItem.incomeDetails,
                                        expenseDetails = null,
                                        category = Category()
                                    )
                                }
                            }

                            is MovementItem.ExpenseItem -> {
                                Card(
                                    onClick = {
                                        homeViewModel?.onMovementSelected(
                                            movementItem.expenseDetails.toHistoryMovementItem()
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                                    )
                                ) {
                                    MovementCard(
                                        incomeDetails = null,
                                        expenseDetails = movementItem.expenseDetails,
                                        category = Category()
                                    )
                                }
                            }

                            is MovementItem.SaveItem -> {
                                Card(
                                    onClick = {
                                        homeViewModel?.onMovementSelected(
                                            movementItem.saveDetails.toHistoryMovementItem()
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                                    )
                                ) {
                                    MovementCard(
                                        incomeDetails = null,
                                        expenseDetails = null,
                                        saveDetails = movementItem.saveDetails,
                                        category = Category()
                                    )
                                }
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 20.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.background.copy(alpha = 0f),
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                                    MaterialTheme.colorScheme.background,
                                    MaterialTheme.colorScheme.background,
                                )
                            ),
                            shape = RectangleShape
                        )
                )
            }
        }

        homeState?.selectedMovement?.let { movement ->
            val movementId = when (movement) {
                is HistoryMovementItem.IncomeItem -> movement.incomeDetails.movement.id
                is HistoryMovementItem.ExpenseItem -> movement.expenseDetails.movement.id
                is HistoryMovementItem.SaveItem -> movement.saveDetails.movement.id
            }

            MovementDetailsModal(
                movement = movement,
                onDismissRequest = { homeViewModel?.onMovementSelected(null) },
                onDelete = if (movementsViewModel != null && userId.isNotEmpty()) {
                    {
                        movementsViewModel.deleteMovement(
                            movementId = movementId,
                            onSuccess = {
                                homeViewModel?.onMovementSelected(null)
                            },
                            onError = { error ->
                                // TODO: Show error to user
                                homeViewModel?.onMovementSelected(null)
                            }
                        )
                    }
                } else null
            )
        }
    }
}

@Composable
fun BalanceCard(
    currentBalance: Double = 0.0,
    income: Double = 0.0,
    outcome: Double = 0.0,
    currency: String = CurrencyUtils.getCurrencyByCountry()
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = stringResource(R.string.home_current_balance),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "$currency ${String.format(Locale.getDefault(), "%.2f", currentBalance)}",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val incomePrefix = stringResource(R.string.home_income_prefix)
                val expensesPrefix = stringResource(R.string.home_expenses_prefix)
                Text(
                    text = "$incomePrefix ${
                        String.format(
                            Locale.getDefault(),
                            "%.2f",
                            income
                        )
                    }",
                    fontSize = 12.sp,
                    color = MaterialTheme.soleaColors.income,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "$expensesPrefix $currency ${
                        String.format(
                            Locale.getDefault(),
                            "%.2f",
                            outcome
                        )
                    }",
                    fontSize = 12.sp,
                    color = MaterialTheme.soleaColors.expense,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ChartCard(
    income: Double = 0.0,
    expenses: Double = 0.0,
    currency: String = CurrencyUtils.getCurrencyByCountry(),
    onClick: () -> Unit = {}
) {
    val incomeColor = MaterialTheme.soleaColors.income
    val expenseColor = MaterialTheme.soleaColors.expense
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(income, expenses) {
        modelProducer.runTransaction {
            lineSeries {
                series(x = listOf(1, 2), y = listOf(income, income))
                series(x = listOf(1, 2), y = listOf(expenses, expenses))
            }
        }
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = stringResource(R.string.chart_title),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberLineCartesianLayer(
                        lineProvider = LineCartesianLayer.LineProvider.series(
                            rememberLine(
                                fill = remember(incomeColor) {
                                    LineCartesianLayer.LineFill.single(
                                        fill(incomeColor)
                                    )
                                },
                                thickness = 3.dp,
                                pointProvider = LineCartesianLayer.PointProvider.single(
                                    LineCartesianLayer.Point(
                                        component = rememberShapeComponent(
                                            shape = Shape.Pill,
                                            color = incomeColor
                                        ),
                                        sizeDp = 8f
                                    )
                                )
                            ),
                            rememberLine(
                                fill = remember(expenseColor) {
                                    LineCartesianLayer.LineFill.single(
                                        fill(expenseColor)
                                    )
                                },
                                thickness = 3.dp,
                                pointProvider = LineCartesianLayer.PointProvider.single(
                                    LineCartesianLayer.Point(
                                        component = rememberShapeComponent(
                                            shape = Shape.Pill,
                                            color = expenseColor
                                        ),
                                        sizeDp = 8f
                                    )
                                )
                            )
                        )
                    ),
                    startAxis = rememberStartAxis(
                        line = null,
                        label = com.patrykandpatrick.vico.compose.common.component.rememberTextComponent(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ),
                    bottomAxis = rememberBottomAxis(
                        line = null,
                        label = com.patrykandpatrick.vico.compose.common.component.rememberTextComponent(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                ),
                modelProducer = modelProducer,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(incomeColor, CircleShape)
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.Incomes),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$currency ${
                                String.format(
                                    Locale.getDefault(),
                                    "%.2f",
                                    income
                                )
                            }",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = incomeColor
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(expenseColor, CircleShape)
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.expenses),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$currency ${
                                String.format(
                                    Locale.getDefault(),
                                    "%.2f",
                                    expenses
                                )
                            }",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = expenseColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AlertCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    amount: String,
    percentage: String,
    backgroundColor: Color,
    contentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconColor.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    color = contentColor,
                    lineHeight = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = amount,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = percentage,
                    fontSize = 12.sp,
                    color = if (percentage.startsWith("+")) MaterialTheme.colorScheme.error else iconColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    SoleaTheme(
        darkTheme = true
    ) {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            HomeScreenContent()
        }
    }
}