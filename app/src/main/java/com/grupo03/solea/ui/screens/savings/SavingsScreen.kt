package com.grupo03.solea.ui.screens.savings

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo03.solea.data.models.SavingsGoal
import com.grupo03.solea.presentation.viewmodels.screens.BudgetViewModel
import com.grupo03.solea.presentation.viewmodels.screens.SavingsViewModel
import com.grupo03.solea.presentation.viewmodels.shared.AuthViewModel
import com.grupo03.solea.presentation.viewmodels.shared.MovementsViewModel
import com.grupo03.solea.ui.components.SectionTitle
import com.grupo03.solea.ui.components.TopBar
import com.grupo03.solea.utils.CurrencyUtils
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.grupo03.solea.R
import com.grupo03.solea.ui.components.AddMoneyToGoalDialog

@Composable
fun SavingsScreen(
    authViewModel: AuthViewModel,
    budgetViewModel: BudgetViewModel,
    movementsViewModel: MovementsViewModel,
    savingsViewModel: SavingsViewModel,
    onNavigateToBudgetLimits: () -> Unit,
    onNavigateToGoalManagement: () -> Unit,
    onNavigateToEditGoal: (SavingsGoal) -> Unit, // <-- New parameter
    onEditBudget: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val budgetState = budgetViewModel.budgetState.collectAsState()
    val movementsState = movementsViewModel.movementsState.collectAsState()
    val authState = authViewModel.authState.collectAsState()
    val savingsState = savingsViewModel.uiState.collectAsState()
    val user = authState.value.user
    var selectedGoal by remember { mutableStateOf<SavingsGoal?>(null) }
    var showAddMoneyDialog by remember { mutableStateOf(false) }

    LaunchedEffect(user) {
        if (user != null) {
            movementsViewModel.observeMovements(user.uid)
            movementsViewModel.observeCategories(user.uid)
            budgetViewModel.fetchBudgetsAndCategories(user.uid)
            savingsViewModel.observeGoals(user.uid)
        }
    }

    val spendingByCategory = remember(movementsState.value.expenseDetailsList) {
        movementsState.value.expenseDetailsList
            .groupBy { it.movement.category }
            .mapValues { (_, expenseDetails) -> expenseDetails.sumOf { it.movement.total } }
    }

    Column(modifier = modifier.fillMaxSize()) {
        TopBar(title = "Ahorros")

        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionTitle(text = "Metas de Ahorro", icon = Icons.Default.Savings)
                Spacer(modifier = Modifier.height(4.dp))
            }

            val activeGoals = savingsState.value.goals.filter { it.isActive }
            items(activeGoals, key = { it.id }) { goal ->
                GoalCard(
                    goal = goal,
                    onAddMoneyClick = {
                        selectedGoal = goal
                        showAddMoneyDialog = true
                    },
                    onEditClick = { onNavigateToEditGoal(goal) },
                    onDeactivateClick = { savingsViewModel.deactivateGoal(goal.id) },
                    onCompleteClick = { savingsViewModel.markAsCompleted(goal.id) }
                )
            }

            item {
                AddCard(onClick = onNavigateToGoalManagement)
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                SectionTitle(text = "Límites de Gastos", icon = Icons.Default.Receipt)
                Spacer(modifier = Modifier.height(4.dp))
            }

            val activeBudgets = budgetState.value.budgets.filter { it.statusId != "inactive" && it.statusId.isNotEmpty() }
            items(activeBudgets, key = { it.id }) { budget ->
                val spent: Double = spendingByCategory[budget.category] ?: 0.0
                val percent = if (budget.amount > 0.0) ((spent / budget.amount) * 100.0).toInt() else 0

                LimitCard(
                    name = budget.category,
                    percent = percent,
                    spent = spent,
                    limit = budget.amount,
                    color = when {
                        percent >= 100 -> Color(0xFFF44336)
                        percent >= 80 -> Color(0xFFFFC107)
                        else -> Color(0xFF4CAF50)
                    },
                    onEditClick = { onEditBudget(budget.category) },
                    onDeactivateClick = { user?.uid?.let { budgetViewModel.deleteBudget(it, budget.id) {} } }
                )
            }

            item {
                AddCard(onClick = onNavigateToBudgetLimits)
            }
        }

        if (showAddMoneyDialog && selectedGoal != null) {
            val currentBalance = movementsState.value.balance
            AddMoneyToGoalDialog(
                goal = selectedGoal!!,
                availableBalance = currentBalance,
                onDismiss = { showAddMoneyDialog = false },
                onConfirm = { amount ->
                    val userUid = authState.value.user?.uid ?: return@AddMoneyToGoalDialog

                    savingsViewModel.addMoneyToGoal(
                        userUid = userUid,
                        goalId = selectedGoal!!.id,
                        amount = amount,
                        currentBalance = currentBalance
                    )

                    savingsViewModel.updateUiAfterAddingMoney(selectedGoal!!.id, amount)

                    showAddMoneyDialog = false

                }

            )
        }

    }
}

@Composable
fun GoalCard(
    goal: SavingsGoal,
    onAddMoneyClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeactivateClick: () -> Unit,
    onCompleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val progress = goal.progress()
    val color = when {
        goal.isCompleted -> Color(0xFF4CAF50)
        progress >= 1f -> Color(0xFF4CAF50)
        progress >= 0.7f -> Color(0xFF2196F3)
        else -> MaterialTheme.colorScheme.primary
    }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMMM, yyyy", Locale.getDefault()) }
    val formattedDate = goal.deadline.atZone(java.time.ZoneId.systemDefault()).toLocalDate().format(dateFormatter)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Savings,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(goal.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${goal.progressPercentage}%", color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        color = color,
                        trackColor = color.copy(alpha = 0.2f),
                        modifier = Modifier.height(8.dp).weight(1f).clip(RoundedCornerShape(4.dp))
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val currencySymbol = CurrencyUtils.getCurrencySymbol(goal.currency)
                    Text(
                        text = "%.2f / %.2f".format(Locale.getDefault(), goal.currentAmount, goal.targetAmount),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "Vence: $formattedDate",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                    )
                }
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.options),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.goal_edit)) },
                        onClick = { onEditClick(); showMenu = false })
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.goal_save)) },
                        onClick = { onAddMoneyClick(); showMenu = false })
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.goal_complete)) },
                        onClick = { onCompleteClick(); showMenu = false },
                        enabled = !goal.isCompleted
                    )
                    /*DropdownMenuItem(
                        text = { Text(stringResource(R.string.goal_deactivate)) },
                        onClick = { onDeactivateClick(); showMenu = false })*/
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LimitCard(
    name: String,
    percent: Int,
    spent: Double,
    limit: Double,
    color: Color,
    onEditClick: () -> Unit,
    onDeactivateClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Status indicator bar
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(90.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                    .padding(end = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 8.dp, horizontal = 0.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .run {
                            background(color)
                        }
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .run {
                                background(color.copy(alpha = 0.15f))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                percent >= 100 -> Icons.Default.Warning
                                percent >= 80 -> Icons.Outlined.Info
                                else -> Icons.Default.CheckCircle
                            },
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val currencySymbol = CurrencyUtils.getDeviceCurrencySymbol()
                        Text(
                            text = "%s %.2f de %s %.2f".format(
                                Locale.getDefault(),
                                currencySymbol,
                                spent,
                                currencySymbol,
                                limit
                            ),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.options),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.saving_edit_limits)) },
                                onClick = {
                                    showMenu = false
                                    onEditClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.savings_deactivate_limit)) },
                                onClick = {
                                    showMenu = false
                                    onDeactivateClick()
                                }
                            )
                        }
                    }
                }

                // Progress bar
                Column(
                    modifier = Modifier.padding(start = 12.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "$percent%",
                            color = color,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        LinearProgressIndicator(
                            progress = { (percent.coerceAtMost(100) / 100f) },
                            color = color,
                            trackColor = color.copy(alpha = 0.2f),
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddCard(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = stringResource(R.string.add),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.add_new),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
        }
    }
}
