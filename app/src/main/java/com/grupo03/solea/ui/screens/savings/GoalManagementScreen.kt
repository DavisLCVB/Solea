package com.grupo03.solea.ui.screens.savings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.grupo03.solea.data.models.SavingsGoal
import com.grupo03.solea.presentation.viewmodels.screens.SavingsViewModel
import com.grupo03.solea.utils.CurrencyUtils
import java.util.Locale
import com.grupo03.solea.R

@Composable
fun GoalManagementScreen(
    savingsViewModel: SavingsViewModel,
    onNavigateToNewGoal: () -> Unit,
    onNavigateToEditGoal: (SavingsGoal) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val savingsState by savingsViewModel.uiState.collectAsState()

    val activeGoals = savingsState.goals.filter { it.isActive && !it.isCompleted }
    val completedGoals = savingsState.goals.filter { it.isCompleted }

    Column(modifier = modifier.fillMaxSize()) {
        // --- Top Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.goal_management),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = onNavigateToNewGoal) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_new))
            }
        }

        HorizontalDivider()

        // --- Lists ---
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Active Goals
            if (activeGoals.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.goal_actives),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(activeGoals, key = { it.id }) {
                    GoalListItem(goal = it, onClick = { onNavigateToEditGoal(it) })
                }
            }

            // Spacer between sections
            if (activeGoals.isNotEmpty() && completedGoals.isNotEmpty()) {
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            // Completed Goals
            if (completedGoals.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.goal_completed),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(completedGoals, key = { it.id }) {
                    GoalListItem(goal = it, onClick = null) // Not clickable
                }
            }
        }
    }
}

@Composable
private fun GoalListItem(
    goal: SavingsGoal,
    onClick: (() -> Unit)?
) {
    val currencySymbol = CurrencyUtils.getCurrencySymbol(goal.currency)
    val progressText = "%.2f / %.2f".format(Locale.getDefault(), goal.currentAmount, goal.targetAmount)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Savings,
                contentDescription = "Icono de Meta",
                tint = if (goal.isCompleted) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = goal.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$currencySymbol$progressText",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (onClick != null) {
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = stringResource(R.string.edit))
            }
        }
    }
}
