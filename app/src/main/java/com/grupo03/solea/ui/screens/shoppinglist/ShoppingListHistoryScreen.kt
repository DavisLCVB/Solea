package com.grupo03.solea.ui.screens.shoppinglist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo03.solea.R
import com.grupo03.solea.data.models.ShoppingListStatus
import com.grupo03.solea.presentation.viewmodels.screens.ShoppingViewModel
import com.grupo03.solea.ui.components.TopBar
import com.grupo03.solea.utils.CurrencyUtils
import org.koin.compose.koinInject
import java.util.Locale
import com.grupo03.solea.data.repositories.interfaces.ShoppingListRepository
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListHistoryScreen(
    shoppingViewModel: ShoppingViewModel = koinInject(),
    authViewModel: com.grupo03.solea.presentation.viewmodels.shared.AuthViewModel = koinInject(),
    onNavigateBack: () -> Unit,
    onNavigateToViewList: (String) -> Unit = {}
) {
    val uiState by shoppingViewModel.uiState.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val userId = authState.user?.uid ?: ""
    val userCurrency = authState.user?.currency ?: "USD"

    LaunchedEffect(Unit) {
        shoppingViewModel.fetchShoppingListsHistory(userId)
    }

    Scaffold(
        topBar = {
            TopBar(
                title = "Historial de Listas",
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.shoppingListsHistory.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No hay listas en el historial",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.shoppingListsHistory) { list ->
                        ShoppingListHistoryCardWithTotal(
                            shoppingList = list,
                            shoppingViewModel = shoppingViewModel,
                            userCurrency = userCurrency,
                            onItemClick = {
                                onNavigateToViewList(list.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShoppingListHistoryCard(
    shoppingList: com.grupo03.solea.data.models.ShoppingList,
    totalAmount: Double = 0.0,
    userCurrency: String = "USD",
    onItemClick: () -> Unit
) {
    val statusText = when (shoppingList.status) {
        ShoppingListStatus.ARCHIVED -> "Completada"
        ShoppingListStatus.CANCELLED -> "Desestimada"
        ShoppingListStatus.ACTIVE -> "Activa"
    }

    val statusColor = when (shoppingList.status) {
        ShoppingListStatus.ARCHIVED -> MaterialTheme.colorScheme.primary // Verde
        ShoppingListStatus.CANCELLED -> MaterialTheme.colorScheme.onSurfaceVariant // Plomo/gris
        ShoppingListStatus.ACTIVE -> MaterialTheme.colorScheme.secondary
    }

    val currencySymbol = CurrencyUtils.getCurrencySymbol(userCurrency)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        onClick = onItemClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = shoppingList.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor
                    )
                }
            }
            // Fechas de creación / archivo se omiten aquí para evitar dependencias
            // con APIs de fecha que requieren niveles de API más altos.
            if (totalAmount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Total: %s %.2f".format(Locale.getDefault(), currencySymbol, totalAmount),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ShoppingListHistoryCardWithTotal(
    shoppingList: com.grupo03.solea.data.models.ShoppingList,
    shoppingViewModel: ShoppingViewModel,
    userCurrency: String = "USD",
    onItemClick: () -> Unit
) {
    var totalAmount by remember { mutableStateOf(0.0) }
    val shoppingListRepository: ShoppingListRepository = koinInject()
    val scope = rememberCoroutineScope()

    LaunchedEffect(shoppingList.id) {
        // Fetch items to calculate total
        scope.launch {
            val itemsResult = shoppingListRepository.getShoppingItemsByListId(shoppingList.id)
            if (itemsResult is com.grupo03.solea.utils.RepositoryResult.Success) {
                totalAmount = itemsResult.data
                    .filter { it.isBought && it.realPrice != null }
                    .sumOf { it.realPrice ?: 0.0 }
            }
        }
    }

    ShoppingListHistoryCard(
        shoppingList = shoppingList,
        totalAmount = totalAmount,
        userCurrency = userCurrency,
        onItemClick = onItemClick
    )
}

