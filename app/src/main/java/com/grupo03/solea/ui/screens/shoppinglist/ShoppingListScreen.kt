package com.grupo03.solea.ui.screens.shoppinglist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
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
fun ShoppingListScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Divider(thickness = 2.dp, color = MaterialTheme.colorScheme.surfaceVariant)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Compras para el cumple de Paquito",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { /* TODO: Editar lista */ }) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }
        }
        ShoppingItemCard("Pickeo Snacks", 6.50, 0, 1, Color(0xFFB0BEC5))
        Spacer(modifier = Modifier.height(8.dp))
        ShoppingItemCard("Doritos", 5.50, 3, 6, Color(0xFFFFEB3B))
        Spacer(modifier = Modifier.height(8.dp))
        ShoppingItemCard("Galleta Oreo", 6.50, 7, 7, Color(0xFF4CAF50))
        Spacer(modifier = Modifier.height(8.dp))
        Divider(thickness = 2.dp, color = MaterialTheme.colorScheme.surfaceVariant)
        AddCard()
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        FloatingActionButton(
            onClick = { /* TODO: Add new item */ },
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Agregar", tint = Color.White)
        }
    }
}

@Composable
fun ShoppingItemCard(name: String, price: Double, current: Int, total: Int, statusColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "S/ %.2f".format(price),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = "$current/$total",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 8.dp)
            )
            Icon(
                imageVector = when {
                    current == total -> Icons.Default.CheckCircle
                    current == 0 -> Icons.Default.CheckCircle
                    else -> Icons.Default.CheckCircle
                },
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(28.dp)
            )
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
