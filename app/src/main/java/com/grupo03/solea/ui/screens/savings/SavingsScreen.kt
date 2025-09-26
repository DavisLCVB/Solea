package com.grupo03.solea.ui.screens.savings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SavingsScreen(modifier: Modifier = Modifier) {
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
        AddCard()
        Text(
            text = "Límites de gastos",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
        )
        LimitCard(
            name = "Snacks",
            percent = 80,
            amount = 50.0,
            color = Color(0xFFF44336)
        )
        Spacer(modifier = Modifier.height(8.dp))
        LimitCard(
            name = "Alcohol",
            percent = 10,
            amount = 40.0,
            color = Color(0xFF4CAF50)
        )
        Spacer(modifier = Modifier.height(8.dp))
        AddCard()
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        FloatingActionButton(
            onClick = { /* TODO: Add new goal or limit */ },
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Agregar", tint = Color.White)
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
                        progress = percent / 100f,
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

@Composable
fun LimitCard(name: String, percent: Int, amount: Double, color: Color) {
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
                imageVector = Icons.Default.Close,
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
                        progress = percent / 100f,
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
            }
            IconButton(onClick = { }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
            }
        }
    }
}

@Composable
fun AddCard() {
    Card(
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
