package com.grupo03.solea.ui.screens.shoppinglist

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.grupo03.solea.R
import com.grupo03.solea.data.models.ShoppingItem
import com.grupo03.solea.presentation.viewmodels.screens.ShoppingViewModel
import com.grupo03.solea.ui.components.SectionTitle
import com.grupo03.solea.ui.components.TopBar
import com.grupo03.solea.utils.CurrencyUtils
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    shoppingViewModel: ShoppingViewModel = koinInject(),
    authViewModel: com.grupo03.solea.presentation.viewmodels.shared.AuthViewModel = koinInject(),
    onNavigateToEditList: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToNewMovement: (ShoppingItem?, Double?) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val uiState by shoppingViewModel.uiState.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val userId = authState.user?.uid ?: ""
    val userCurrency = authState.user?.currency ?: "PEN"
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            shoppingViewModel.initializeRecorder(context)
            shoppingViewModel.observeActiveList(userId)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        shoppingViewModel.setPermissionGranted(isGranted)
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    // Mostrar errores como texto legible (con mensaje detallado si está disponible)
    val errorMessage = uiState.error?.let { error ->
        // Si hay un mensaje detallado, usarlo; si no, usar el mensaje genérico del error
        uiState.errorMessage ?: stringResource(error.messageRes)
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    // Check if list is complete
    LaunchedEffect(uiState.activeList) {
        uiState.activeList?.let { list ->
            if (list.items.isNotEmpty() && list.items.all { it.isBought }) {
                // List is complete - could show dialog here
            }
        }
    }

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var editingName by remember { mutableStateOf("") }

    val activeList = uiState.activeList
    
    Scaffold(
        topBar = {
            Column {
                // Top bar principal - siempre visible
                TopBar(
                    title = stringResource(R.string.shopping_list_title),
                    actions = {
                        // History button - siempre visible
                        IconButton(onClick = onNavigateToHistory) {
                            Icon(Icons.Default.History, contentDescription = "Historial")
                        }
                    }
                )
                
                // Segundo top bar - solo cuando hay lista activa
                activeList?.let { list ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = list.shoppingList.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )

                        // Edit name button
                        IconButton(onClick = {
                            editingName = list.shoppingList.name
                            showEditNameDialog = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar nombre")
                        }

                        // Menu button con 3 puntos
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Editar") },
                                    onClick = {
                                        showMenu = false
                                        onNavigateToEditList()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Desestimar") },
                                    onClick = {
                                        showMenu = false
                                        showCancelDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        showMenu = false
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        },
        floatingActionButton = {
            val isEnabled = activeList == null || uiState.isRecording
            FloatingActionButton(
                onClick = {
                    if (isEnabled) {
                        if (uiState.isRecording) {
                            shoppingViewModel.stopRecordingAndAnalyze(userId)
                        } else {
                            shoppingViewModel.startRecording()
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(16.dp)
                    .then(
                        if (!isEnabled) {
                            Modifier.alpha(0.6f)
                        } else {
                            Modifier
                        }
                    )
            ) {
                Icon(
                    if (uiState.isRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (uiState.isRecording) "Detener" else "Grabar",
                    tint = Color.White
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isProcessingVoice -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Analizando audio...")
                        }
                    }
                }
                uiState.isRecording -> {
                    RecordingIndicator(
                        duration = uiState.recordingDuration,
                        onCancel = { shoppingViewModel.cancelRecording() }
                    )
                }
                uiState.activeList == null -> {
                    EmptyListContent(
                        onRecordClick = { shoppingViewModel.startRecording() }
                    )
                }
                else -> {
                    ActiveListContent(
                        shoppingListDetails = uiState.activeList!!,
                        userCurrency = userCurrency,
                        onItemChecked = { item, checked ->
                            if (checked) {
                                // Navigate to NewMovementFormScreen with pre-filled data
                                onNavigateToNewMovement(item, item.estimatedPrice)
                            }
                        }
                    )
                }
            }
        }
    }

    // Voice preview dialog
    if (uiState.showVoicePreviewDialog && uiState.analyzedVoiceData != null) {
        VoicePreviewDialog(
            voiceData = uiState.analyzedVoiceData!!,
            onConfirm = {
                shoppingViewModel.createListFromAnalyzedVoice(
                    userUid = userId,
                    listName = null,
                    onSuccess = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Lista creada exitosamente")
                        }
                    }
                )
            },
            onDismiss = { shoppingViewModel.closeVoicePreviewDialog() }
        )
    }

    // Delete dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Lista") },
            text = { Text("¿Estás seguro de que deseas eliminar esta lista? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        uiState.activeList?.shoppingList?.id?.let { listId ->
                            shoppingViewModel.deleteList(
                                listId = listId,
                                onSuccess = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Lista eliminada")
                                    }
                                },
                                onError = { error ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar(error)
                                    }
                                }
                            )
                        }
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Cancel dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Desestimar Lista") },
            text = { 
                Text("¿Estás seguro de que deseas desestimar esta lista? La lista quedará en el historial pero ya no estará activa para hacer matching automático con tus gastos.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelDialog = false
                        uiState.activeList?.shoppingList?.id?.let { listId ->
                            shoppingViewModel.cancelList(
                                listId = listId,
                                onSuccess = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Lista desestimada")
                                    }
                                },
                                onError = { error ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar(error)
                                    }
                                }
                            )
                        }
                    }
                ) {
                    Text("Desestimar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Edit name dialog
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Editar Nombre de Lista") },
            text = {
                Column {
                    Text("Ingresa un nuevo nombre para la lista:")
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = editingName,
                        onValueChange = { editingName = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showEditNameDialog = false
                        if (editingName.isNotBlank()) {
                            uiState.activeList?.shoppingList?.id?.let { listId ->
                                shoppingViewModel.updateListName(
                                    listId = listId,
                                    newName = editingName.trim(),
                                    onSuccess = {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Nombre actualizado")
                                        }
                                    },
                                    onError = { error ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar(error)
                                        }
                                    }
                                )
                            }
                        }
                    },
                    enabled = editingName.isNotBlank()
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun RecordingIndicator(
    duration: Long,
    onCancel: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Mic,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = formatDuration(duration),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Grabando...")
            Spacer(modifier = Modifier.height(24.dp))
            TextButton(onClick = onCancel) {
                Text("Cancelar")
            }
        }
    }
}

@Composable
fun EmptyListContent(onRecordClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No hay lista activa",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Presiona el micrófono para crear una lista de compras",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ActiveListContent(
    shoppingListDetails: com.grupo03.solea.data.models.ShoppingListDetails,
    userCurrency: String = "PEN",
    onItemChecked: (ShoppingItem, Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle(
            text = shoppingListDetails.shoppingList.name,
            icon = Icons.Default.ShoppingCart
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(shoppingListDetails.items) { item ->
                ShoppingItemCard(
                    item = item,
                    userCurrency = userCurrency,
                    onCheckedChange = { checked ->
                        onItemChecked(item, checked)
                    }
                )
            }
        }
    }
}

@Composable
fun ShoppingItemCard(
    item: com.grupo03.solea.data.models.ShoppingItem,
    userCurrency: String = "PEN",
    onCheckedChange: (Boolean) -> Unit
) {
    val currencySymbol = CurrencyUtils.getCurrencySymbol(userCurrency)
    val statusColor = when {
        item.isBought -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isBought,
                onCheckedChange = onCheckedChange
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = if (item.isBought) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
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
            if (item.isBought) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun VoicePreviewDialog(
    voiceData: com.grupo03.solea.data.models.ShoppingListVoiceResponse,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Vista previa de la lista",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = voiceData.shoppingList.listName ?: "Lista de compras",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Items detectados:",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                voiceData.shoppingList.items.forEach { item ->
                    Text(
                        text = "• ${item.name} (${item.quantity})",
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onConfirm) {
                        Text("Crear Lista")
                    }
                }
            }
        }
    }
}

fun formatDuration(seconds: Long): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}
