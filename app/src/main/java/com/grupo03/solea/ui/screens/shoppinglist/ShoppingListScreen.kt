package com.grupo03.solea.ui.screens.shoppinglist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo03.solea.R
import com.grupo03.solea.ui.components.SectionTitle
import com.grupo03.solea.ui.components.TopBar
import com.grupo03.solea.utils.CurrencyUtils
import java.util.Locale

@Composable
fun ShoppingListScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        TopBar(
            title = stringResource(R.string.shopping_list_title),
            actions = {
                IconButton(onClick = { /* TODO: Editar lista */ }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
            }
        )

        Column(
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            SectionTitle(
                text = "Compras para el cumple de Paquito",
                icon = Icons.Default.ShoppingCart
            )
            Spacer(modifier = Modifier.height(12.dp))
            ShoppingItemCard("Pickeo Snacks", 6.50, 0, 1, Color(0xFFB0BEC5))
            Spacer(modifier = Modifier.height(8.dp))
            ShoppingItemCard("Doritos", 5.50, 3, 6, Color(0xFFFFEB3B))
            Spacer(modifier = Modifier.height(8.dp))
            ShoppingItemCard("Galleta Oreo", 6.50, 7, 7, Color(0xFF4CAF50))
            Spacer(modifier = Modifier.height(12.dp))
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
            val currencySymbol = CurrencyUtils.getDeviceCurrencySymbol()
            Text(
                text = "%s %.2f".format(Locale.getDefault(), currencySymbol, price),
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
            Icon(
                Icons.Default.Add,
                contentDescription = stringResource(R.string.add),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
