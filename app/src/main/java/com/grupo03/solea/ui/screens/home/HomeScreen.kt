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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo03.solea.data.models.Movement
import com.grupo03.solea.data.models.MovementType
import com.grupo03.solea.presentation.states.CoreState
import com.grupo03.solea.presentation.viewmodels.AuthViewModel
import com.grupo03.solea.presentation.viewmodels.CoreViewModel
import com.grupo03.solea.ui.components.MovementModalBottomSheet
import com.grupo03.solea.ui.theme.SoleaTheme
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    coreViewModel: CoreViewModel
) {
    val coreState = coreViewModel.uiState.collectAsState()
    val screenState = coreState.value.homeScreenState
    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        HomeScreenContent(
            modifier = Modifier.fillMaxSize(),
            onAddClick = coreViewModel::onActivateSheet,
            screenState = screenState,
        )
        if (screenState.activeSheet) {
            MovementModalBottomSheet(
                onDismissRequest = coreViewModel::onDeactivateSheet
            )
        }
    }

}

@Composable
fun Separator(
    title: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title)
        Spacer(modifier = Modifier.width(10.dp))
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit = { },
    screenState: CoreState.HomeScreenState = CoreState.HomeScreenState(),
) {
    Box() {
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Balance Card
            BalanceCard(
                currentBalance = screenState.balance,
                income = screenState.income,
                outcome = screenState.outcome
            )

            // Income vs Expenses Chart Placeholder
            ChartCard()

            // Separator
            Separator(title = "Ultimos movimientos")

            // Alerts/Notifications
            //AlertsSection(financialData = financialData)

            // Recent Movements
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                screenState.movementSet.take(5).forEach { (movement, movementType) ->
                    MovementCard(
                        movement = movement,
                        movementType = movementType
                    )
                }
            }

            // Add some bottom spacing
            Spacer(modifier = Modifier.height(16.dp))
        }
        FloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add")
        }
    }
}

@Composable
fun MovementCard(
    movement: Movement,
    movementType: MovementType
) {
    val zone = ZoneId.systemDefault()
    val format = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm").withZone(zone)
    val dateFormated = format.format(movement.date)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ) {
            Text(
                text = dateFormated,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = movement.amount.toString(),
                modifier = Modifier.weight(0.5f),
                textAlign = TextAlign.Center
            )
            Text(
                text = movementType.value,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
fun BalanceCard(
    currentBalance: Double = 0.0,
    income: Double = 0.0,
    outcome: Double = 0.0,
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
                text = "Saldo actual",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "S/ ${String.format("%.2f", currentBalance)}",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Ingresos: S/ ${String.format("%.2f", income)}",
                    fontSize = 12.sp,
                    color = Color(0xFF4CAF50), // Green for income
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "Gastos: S/ ${String.format("%.2f", outcome)}",
                    fontSize = 12.sp,
                    color = Color(0xFFF44336), // Red for expenses
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ChartCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
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
                text = "Ingresos vs gastos",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Placeholder for chart - you can replace this with actual chart later
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Gray.copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Gráfico de ingresos vs gastos\n(Por implementar)",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
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