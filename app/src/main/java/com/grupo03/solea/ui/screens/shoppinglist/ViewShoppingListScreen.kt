package com.grupo03.solea.ui.screens.shoppinglist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo03.solea.data.models.ShoppingListStatus
import com.grupo03.solea.presentation.viewmodels.screens.ShoppingViewModel
import com.grupo03.solea.ui.components.SectionTitle
import com.grupo03.solea.ui.components.TopBar
import com.grupo03.solea.utils.CurrencyUtils
import org.koin.compose.koinInject
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewShoppingListScreen(
    listId: String,
    shoppingViewModel: ShoppingViewModel = koinInject(),
    authViewModel: com.grupo03.solea.presentation.viewmodels.shared.AuthViewModel = koinInject(),
    onNavigateBack: () -> Unit
) {
    val uiState by shoppingViewModel.uiState.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val userCurrency = authState.user?.currency ?: "PEN"

    LaunchedEffect(listId) {
        shoppingViewModel.fetchShoppingListDetails(listId) {
            // Details loaded
        }
    }

    val listDetails = uiState.viewedListDetails

    Scaffold(
        topBar = {
            TopBar(
                title = listDetails?.shoppingList?.name ?: "Lista de compras",
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (listDetails == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Lista no encontrada")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 12.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Status badge
                val statusText = when (listDetails.shoppingList.status) {
                    ShoppingListStatus.ARCHIVED -> "Completada"
                    ShoppingListStatus.CANCELLED -> "Desestimada"
                    ShoppingListStatus.ACTIVE -> "Activa"
                }

                val statusColor = when (listDetails.shoppingList.status) {
                    ShoppingListStatus.ARCHIVED -> MaterialTheme.colorScheme.primary
                    ShoppingListStatus.CANCELLED -> MaterialTheme.colorScheme.onSurfaceVariant
                    ShoppingListStatus.ACTIVE -> MaterialTheme.colorScheme.secondary
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.2f),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor
                    )
                }

                SectionTitle(
                    text = listDetails.shoppingList.name,
                    icon = Icons.Default.ShoppingCart
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Calculate total amount from bought items
                val currencySymbol = CurrencyUtils.getCurrencySymbol(userCurrency)
                val totalAmount = listDetails.items
                    .filter { it.isBought && it.realPrice != null }
                    .sumOf { it.realPrice ?: 0.0 }

                if (totalAmount > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total gastado:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "%s %.2f".format(Locale.getDefault(), currencySymbol, totalAmount),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listDetails.items) { item ->
                        ViewShoppingItemCard(
                            item = item,
                            userCurrency = userCurrency
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ViewShoppingItemCard(
    item: com.grupo03.solea.data.models.ShoppingItem,
    userCurrency: String = "PEN"
) {
    val currencySymbol = CurrencyUtils.getCurrencySymbol(userCurrency)
    val isBought = item.isBought

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isBought) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = if (isBought) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Cantidad: ${item.quantity}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (item.estimatedPrice != null) {
                    Text(
                        text = "Estimado: %s %.2f".format(Locale.getDefault(), currencySymbol, item.estimatedPrice),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (item.realPrice != null) {
                    Text(
                        text = "Real: %s %.2f".format(Locale.getDefault(), currencySymbol, item.realPrice),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (isBought) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

