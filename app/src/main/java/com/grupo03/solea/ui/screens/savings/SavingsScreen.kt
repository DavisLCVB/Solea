package com.grupo03.solea.ui.screens.savings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo03.solea.data.models.Budget
import com.grupo03.solea.data.models.Movement
import com.grupo03.solea.data.models.MovementType
import com.grupo03.solea.presentation.viewmodels.AuthViewModel
import com.grupo03.solea.presentation.viewmodels.CoreViewModel
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import kotlin.math.abs

@Composable
fun SavingsScreen(
    authViewModel: AuthViewModel,
    coreViewModel: CoreViewModel,
    onNavigateToBudgetLimits: () -> Unit,
    onEditBudget: (MovementType) -> Unit,
    modifier: Modifier = Modifier
) {
    val coreState = coreViewModel.uiState.collectAsState()
    val authState = authViewModel.uiState.collectAsState()
    val userId = authState.value.user?.uid ?: ""

    // Cargar datos
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            coreViewModel.fetchMovements(userId)
            coreViewModel.fetchBudgets(userId)
        }
    }

    // Calcular gastos por categoría
    val spendingByCategory = remember(coreState.value.movements) {
        coreState.value.movements
            .filter { it.amount < 0 }
            .groupBy { it.typeId }
            .mapValues { entry -> abs(entry.value.sumOf { it.amount }) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        Text(
            text = "Metas",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        // Aquí irían las metas (Goals) - mock data por ahora
        GoalCard(
            name = "Laptop",
            percent = 50,
            amount = 3200.0,
            date = "21 de Julio, 2025",
            color = Color(0xFF2196F3)
        )
        Spacer(modifier = Modifier.height(8.dp))
        GoalCard(
            name = "PC",
            percent = 20,
            amount = 7200.0,
            date = "11 de Junio, 2026",
            color = Color(0xFFFFC107)
        )
        Spacer(modifier = Modifier.height(8.dp))
        AddCard(onClick = { /* TODO: Agregar meta */ })

        Text(
            text = "Límites de gastos",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
        )

        // Mostrar límites registrados
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            // FILTRAR budgets activos
            val activeBudgets = coreState.value.budgets.filter { budget ->
                budget.statusId != "inactive" && budget.statusId.isNotEmpty()
            }

            items(activeBudgets) { budget ->
                val category = coreState.value.movementTypes.find { it.id == budget.movementTypeId }
                val spent = spendingByCategory[budget.movementTypeId] ?: 0.0
                val percent = if (budget.amount > 0) {
                    ((spent / budget.amount) * 100).toInt()
                } else 0

                if (category != null) {
                    LimitCard(
                        name = category.value,
                        percent = percent,
                        spent = spent,
                        limit = budget.amount,
                        color = when {
                            percent >= 100 -> Color(0xFFF44336)
                            percent >= 80 -> Color(0xFFFFC107)
                            else -> Color(0xFF4CAF50)
                        },
                        onEditClick = { onEditBudget(category) },  // PASAR EL CALLBACK
                        onDeactivateClick = {
                            // TODO: Desactivar budget
                        }
                    )
                }
            }

            item {
                AddCard(onClick = onNavigateToBudgetLimits)
            }
        }
    }


}

@Composable
fun GoalCard(name: String, percent: Int, amount: Double, date: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "$percent%",
                        color = color,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    LinearProgressIndicator(
                        progress = { percent / 100f },
                        color = color,
                        modifier = Modifier
                            .height(6.dp)
                            .width(60.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "S/ %.2f".format(amount),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = date,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
            IconButton(onClick = { }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (percent >= 100) Icons.Default.Close else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "$percent%",
                        color = color,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    LinearProgressIndicator(
                        progress = { (percent.coerceAtMost(100) / 100f) },
                        color = color,
                        modifier = Modifier
                            .height(6.dp)
                            .width(60.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }
                Text(
                    text = "Gastado: S/ %.2f de S/ %.2f".format(spent, limit),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar límite") },
                        onClick = {
                            showMenu = false
                            onEditClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Desactivar límite") },
                        onClick = {
                            showMenu = false
                            onDeactivateClick()
                        }
                    )
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
            .height(36.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = "Agregar", tint = MaterialTheme.colorScheme.primary)
        }
    }
}