package com.grupo03.solea.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo03.solea.R
import com.grupo03.solea.data.models.Category
import com.grupo03.solea.data.models.ExpenseDetails
import com.grupo03.solea.data.models.IncomeDetails
import com.grupo03.solea.data.models.Movement
import com.grupo03.solea.presentation.states.screens.HistoryMovementItem
import com.grupo03.solea.presentation.states.screens.toHistoryMovementItem
import com.grupo03.solea.presentation.states.shared.MovementsState
import com.grupo03.solea.presentation.viewmodels.screens.HomeViewModel
import com.grupo03.solea.presentation.viewmodels.shared.AuthViewModel
import com.grupo03.solea.presentation.viewmodels.shared.MovementsViewModel
import com.grupo03.solea.ui.components.ExpandableFab
import com.grupo03.solea.ui.components.FabMenuItem
import com.grupo03.solea.ui.components.MovementCard
import com.grupo03.solea.ui.components.MovementDetailsModal
import com.grupo03.solea.ui.components.SectionTitle
import com.grupo03.solea.ui.components.TopBar
import com.grupo03.solea.ui.theme.SoleaTheme
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

/**
 * Sealed class to represent any type of movement (Income or Expense)
 */
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    authViewModel: AuthViewModel,
    movementsViewModel: MovementsViewModel,
    onNavigateToNewMovement: () -> Unit,
    onNavigateToNewCategory: () -> Unit,
    onNavigateToScanReceipt: () -> Unit
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
                userId = userId
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
    userId: String = ""
) {
    // Calculate balance from movements
    val totalIncome = movementsState.incomeDetailsList.sumOf { it.movement.total }
    val totalExpense = movementsState.expenseDetailsList.sumOf { it.movement.total }
    val balance = totalIncome - totalExpense

    // Get currency from first movement or default to device currency
    val currency = (movementsState.incomeDetailsList.firstOrNull()?.movement?.currency
        ?: movementsState.expenseDetailsList.firstOrNull()?.movement?.currency
        ?: CurrencyUtils.getCurrencyByCountry())

    // Combine and sort all movements by creation date (most recent first)
    val allMovements = buildList {
        addAll(movementsState.incomeDetailsList.map { MovementItem.IncomeItem(it) })
        addAll(movementsState.expenseDetailsList.map { MovementItem.ExpenseItem(it) })
    }.sortedByDescending { it.movement.datetime }

    Box {
        Column(
            modifier = modifier
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
                currency = currency
            )

            // Section title
            SectionTitle(
                text = stringResource(R.string.home_last_movements),
                icon = Icons.Default.History
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Alerts/Notifications
            //AlertsSection(financialData = financialData)

            // Recent Movements - Show all movements (scrollable within parent column)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
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
                                    category = Category() // TODO: Get actual category from movementsState
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
                                    category = Category() // TODO: Get actual category from movementsState
                                )
                            }
                        }
                    }
                }
            }

            // Add some bottom spacing
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Show movement details modal
        homeState?.selectedMovement?.let { movement ->
            val movementId = when (movement) {
                is HistoryMovementItem.IncomeItem -> movement.incomeDetails.movement.id
                is HistoryMovementItem.ExpenseItem -> movement.expenseDetails.movement.id
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
                    color = Color(0xFF4CAF50), // Green for income
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
                    color = Color(0xFFF44336), // Red for expenses
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
    currency: String = CurrencyUtils.getCurrencyByCountry()
) {
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

            // Gráfico de líneas
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberLineCartesianLayer(
                        lineProvider = LineCartesianLayer.LineProvider.series(
                            rememberLine(
                                fill = remember {
                                    LineCartesianLayer.LineFill.single(
                                        fill(
                                            Color(
                                                0xFF4CAF50
                                            )
                                        )
                                    )
                                },
                                thickness = 3.dp,
                                pointProvider = LineCartesianLayer.PointProvider.single(
                                    LineCartesianLayer.Point(
                                        component = rememberShapeComponent(
                                            shape = Shape.Pill,
                                            color = Color(0xFF4CAF50)
                                        ),
                                        sizeDp = 8f
                                    )
                                )
                            ),
                            rememberLine(
                                fill = remember {
                                    LineCartesianLayer.LineFill.single(
                                        fill(
                                            Color(
                                                0xFFF44336
                                            )
                                        )
                                    )
                                },
                                thickness = 3.dp,
                                pointProvider = LineCartesianLayer.PointProvider.single(
                                    LineCartesianLayer.Point(
                                        component = rememberShapeComponent(
                                            shape = Shape.Pill,
                                            color = Color(0xFFF44336)
                                        ),
                                        sizeDp = 8f
                                    )
                                )
                            )
                        )
                    ),
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis()
                ),
                modelProducer = modelProducer,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Leyenda debajo del gráfico
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Leyenda de Ingresos
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color(0xFF4CAF50), CircleShape)
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
                            color = Color(0xFF4CAF50)
                        )
                    }
                }

                // Leyenda de Gastos
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color(0xFFF44336), CircleShape)
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
                            color = Color(0xFFF44336)
                        )
                    }
                }
            }
        }
    }
}

/*
@Composable
fun AlertsSection(financialData: FinancialData) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Snacks spending alert
        val snacksPercentage = (financialData.snacksSpent / financialData.snacksLimit * 100).toInt()
        AlertCard(
            icon = Icons.Default.Warning,
            iconColor = MaterialTheme.colorScheme.secondary,
            title = "Superaste el 80% del límite en snacks",
            amount = "S/${String.format("%.2f", financialData.snacksSpent)}",
            percentage = "+$snacksPercentage%",
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )

        // Laptop savings progress
        val laptopPercentage = (financialData.laptopSaved / financialData.laptopGoal * 100).toInt()
        AlertCard(
            icon = Icons.Default.CheckCircle,
            iconColor = MaterialTheme.colorScheme.tertiary,
            title = "Llevas ahorrado el 50% de tu meta de laptop",
            amount = "S/${String.format("%.2f", financialData.laptopSaved)}",
            percentage = "-$laptopPercentage%",
            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}
*/

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
            // Warning/Success Icon
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
            // Text content
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
            // Amount and percentage
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