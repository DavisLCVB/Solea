package com.grupo03.solea.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo03.solea.data.models.Movement
import com.grupo03.solea.data.models.MovementType
import com.grupo03.solea.presentation.viewmodels.CoreViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import java.time.temporal.ChronoUnit

// Data classes for grouping movements by date
data class MovementGroup(val title: String, val movements: List<Pair<Movement, MovementType>>)

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    coreViewModel: CoreViewModel
) {
    val coreState = coreViewModel.uiState.collectAsState()
    val movements = coreState.value.movements
    val movementTypes = coreState.value.movementTypes
    
    // Combine movements with their types and sort by date (most recent first)
    val movementSet = movements.mapNotNull { movement ->
        val type = movementTypes.find { it.id == movement.typeId }
        if (type != null) {
            Pair(movement, type)
        } else {
            null
        }
    }.sortedByDescending { it.first.date }
    
    // Group movements by date
    val movementGroups = groupMovementsByDate(movementSet)
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        // Filtros y fecha
        HistoryFilters()
        Spacer(modifier = Modifier.height(8.dp))
        // Lista de transacciones
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            LazyColumn(
                modifier = Modifier.padding(8.dp)
            ) {
                movementGroups.forEach { group ->
                    item {
                        Text(
                            text = group.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 8.dp)
                        )
                    }
                    items(group.movements) { (movement, movementType) ->
                        MovementRow(movement, movementType)
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryFilters() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Transacciones",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.AccountCircle, // Usar un ícono de calendario si lo deseas
            contentDescription = "Calendario",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "23 - 30 Marzo 2025",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 14.sp,
            modifier = Modifier.padding(end = 4.dp)
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf("HOY", "SEMANA", "MES", "AÑO", "PERS.").forEach { label ->
            FilterChip(
                selected = label == "HOY",
                onClick = { /* TODO */ },
                label = { Text(label, fontSize = 12.sp) },
                modifier = Modifier.height(28.dp)
            )
        }
    }
}

@Composable
fun MovementRow(movement: Movement, movementType: MovementType) {
    val isIncome = movement.amount >= 0
    val zone = ZoneId.systemDefault()
    val timeFormat = DateTimeFormatter.ofPattern("HH:mm").withZone(zone)
    val timeFormatted = timeFormat.format(movement.date)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isIncome) Color(0xFFB9F6CA) else Color(0xFFFFEBEE)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isIncome) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = if (isIncome) Color(0xFF00C853) else Color(0xFFD50000),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (movement.item.isNotEmpty()) movement.item else movementType.value,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
            Text(
                text = "${movementType.value}, at $timeFormatted",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = (if (isIncome) "+S/" else "-S/") + String.format("%.2f", kotlin.math.abs(movement.amount)),
            color = if (isIncome) Color(0xFF00C853) else Color(0xFFD50000),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}

// Helper function to group movements by date
@Composable
fun groupMovementsByDate(movementSet: List<Pair<Movement, MovementType>>): List<MovementGroup> {
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)
    
    // Group movements and maintain order (most recent first)
    val grouped = movementSet.groupBy { (movement, _) ->
        val movementDate = movement.date.atZone(ZoneId.systemDefault()).toLocalDate()
        when {
            movementDate == today -> "HOY"
            movementDate == yesterday -> "AYER"
            ChronoUnit.DAYS.between(movementDate, today) <= 7 -> "ESTA SEMANA"
            movementDate.month == today.month && movementDate.year == today.year -> "ESTE MES"
            else -> {
                val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
                movementDate.format(formatter).uppercase()
            }
        }
    }
    
    // Define the order of groups
    val groupOrder = listOf("HOY", "AYER", "ESTA SEMANA", "ESTE MES")
    
    // Create ordered list of groups
    val orderedGroups = mutableListOf<MovementGroup>()
    
    // Add groups in predefined order
    groupOrder.forEach { groupTitle ->
        grouped[groupTitle]?.let { movements ->
            orderedGroups.add(MovementGroup(groupTitle, movements))
        }
    }
    
    // Add any remaining groups (other months/years) sorted by date
    grouped.entries
        .filter { it.key !in groupOrder }
        .sortedByDescending { (_, movements) ->
            movements.maxOfOrNull { it.first.date } ?: java.time.Instant.MIN
        }
        .forEach { (title, movements) ->
            orderedGroups.add(MovementGroup(title, movements))
        }
    
    return orderedGroups
}
