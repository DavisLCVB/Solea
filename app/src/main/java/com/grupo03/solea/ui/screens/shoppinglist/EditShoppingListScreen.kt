package com.grupo03.solea.ui.screens.shoppinglist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo03.solea.R
import kotlinx.coroutines.launch
import com.grupo03.solea.presentation.viewmodels.screens.ShoppingViewModel
import com.grupo03.solea.ui.components.TopBar
import com.grupo03.solea.utils.CurrencyUtils
import org.koin.compose.koinInject
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditShoppingListScreen(
    shoppingViewModel: ShoppingViewModel = koinInject(),
    authViewModel: com.grupo03.solea.presentation.viewmodels.shared.AuthViewModel = koinInject(),
    onNavigateBack: () -> Unit
) {
    val uiState by shoppingViewModel.uiState.collectAsState()
    val userId = authViewModel.authState.collectAsState().value.user?.uid ?: ""
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (!uiState.isEditing) {
            shoppingViewModel.startEditing()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar("Error: ${it.messageRes}")
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = "Editar Lista",
                onBackClick = {
                    shoppingViewModel.cancelEditing()
                    onNavigateBack()
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // List name (solo lectura por ahora)
            OutlinedTextField(
                value = uiState.editingList?.shoppingList?.name ?: "",
                onValueChange = { },
                label = { Text("Nombre de la lista") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false // No permitimos cambiar el nombre por ahora
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Items",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Add new item section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Agregar nuevo item",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.newItemName,
                        onValueChange = shoppingViewModel::updateNewItemName,
                        label = { Text("Nombre del item") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.newItemQuantity,
                            onValueChange = shoppingViewModel::updateNewItemQuantity,
                            label = { Text("Cantidad") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                            ),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = uiState.newItemEstimatedPrice,
                            onValueChange = shoppingViewModel::updateNewItemEstimatedPrice,
                            label = { Text("Precio estimado") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                            ),
                            singleLine = true,
                            prefix = { Text("${CurrencyUtils.getDeviceCurrencySymbol()} ") }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { shoppingViewModel.addItemToEditingList() },
                        enabled = uiState.newItemName.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agregar Item")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Items list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(uiState.editingItems) { item ->
                    ShoppingItemEditCard(
                        item = item,
                        onDelete = {
                            shoppingViewModel.removeItemFromEditingList(item.id)
                        },
                        canDelete = !item.isBought
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        shoppingViewModel.cancelEditing()
                        onNavigateBack()
                    },
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Text("Cancelar", fontSize = 16.sp)
                }

                Button(
                    onClick = {
                        shoppingViewModel.saveEditedList(userId) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Lista actualizada")
                            }
                            onNavigateBack()
                        }
                    },
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Actualizar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ShoppingItemEditCard(
    item: com.grupo03.solea.data.models.ShoppingItem,
    onDelete: () -> Unit,
    canDelete: Boolean
) {
    val currencySymbol = CurrencyUtils.getDeviceCurrencySymbol()
    val isBought = item.isBought

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
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
                .padding(12.dp),
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
                if (isBought) {
                    Text(
                        text = "✓ Comprado",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (canDelete) {
                IconButton(
                    onClick = onDelete,
                    enabled = canDelete
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

