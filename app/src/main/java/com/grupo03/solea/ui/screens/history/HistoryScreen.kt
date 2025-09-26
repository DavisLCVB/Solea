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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Mock data classes
sealed class TransactionType { object Income : TransactionType(); object Expense : TransactionType() }
data class Transaction(
    val name: String,
    val category: String,
    val date: String, // e.g. "12:30 PM"
    val amount: Double,
    val type: TransactionType
)
data class TransactionGroup(val title: String, val transactions: List<Transaction>)

@Composable
fun HistoryScreen(modifier: Modifier = Modifier) {
    val mockGroups = listOf(
        TransactionGroup(
            "HOY", listOf(
                Transaction("Galletas Oreo", "Snacks", "12:30 PM", -3.50, TransactionType.Expense),
                Transaction("Galletas Oreo", "Snacks", "12:30 PM", -3.50, TransactionType.Expense)
            )
        ),
        TransactionGroup(
            "AYER", listOf(
                Transaction("Pickeo Snacks", "Snacks", "12:30 PM", -8.50, TransactionType.Expense),
                Transaction("Inka Chips Naturales", "Snacks", "12:30 PM", -10.90, TransactionType.Expense),
                Transaction("Agua Cielo", "Bebidas", "12:30 PM", -2.10, TransactionType.Expense),
                Transaction("Sueldo", "Sueldo", "12:30 PM", 2000.00, TransactionType.Income),
                Transaction("Pickeo Snacks", "Snacks", "12:30 PM", -8.50, TransactionType.Expense)
            )
        )
    )
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
                mockGroups.forEach { group ->
                    item {
                        Text(
                            text = group.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 8.dp)
                        )
                    }
                    items(group.transactions) { tx ->
                        TransactionRow(tx)
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
fun TransactionRow(tx: Transaction) {
    val isIncome = tx.type is TransactionType.Income
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
                text = tx.name,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
            Text(
                text = "${tx.category}, at ${tx.date}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = (if (isIncome) "+S/" else "-S/") + String.format("%.2f", kotlin.math.abs(tx.amount)),
            color = if (isIncome) Color(0xFF00C853) else Color(0xFFD50000),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}
