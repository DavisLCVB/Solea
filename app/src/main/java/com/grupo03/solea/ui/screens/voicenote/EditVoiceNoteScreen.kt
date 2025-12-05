package com.grupo03.solea.ui.screens.voicenote

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.grupo03.solea.R
import com.grupo03.solea.data.models.Category
import com.grupo03.solea.presentation.viewmodels.screens.AudioAnalysisViewModel
import com.grupo03.solea.presentation.viewmodels.screens.NewMovementFormViewModel
import com.grupo03.solea.presentation.viewmodels.shared.AuthViewModel
import com.grupo03.solea.data.models.MovementType
import com.grupo03.solea.utils.CurrencyUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditVoiceNoteScreen(
    audioAnalysisViewModel: AudioAnalysisViewModel,
    newMovementFormViewModel: NewMovementFormViewModel,
    newCategoryFormViewModel: com.grupo03.solea.presentation.viewmodels.screens.NewCategoryFormViewModel,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val audioState = audioAnalysisViewModel.state.collectAsState()
    val formState = newMovementFormViewModel.formState.collectAsState()
    val userId = authViewModel.authState.collectAsState().value.user!!.uid

    val analyzedVoiceNote = audioState.value.analyzedVoiceNote ?: return

    var amount by remember { mutableStateOf(analyzedVoiceNote.amount) }
    var description by remember { mutableStateOf(analyzedVoiceNote.description) }
    var movementType by remember { mutableStateOf(analyzedVoiceNote.movementType) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var expandedCategory by remember { mutableStateOf(false) }
    var showNewCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var showTranscription by remember { mutableStateOf(false) }
    var suggestedCategoryProcessed by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Check suggested category - wait for categories to be fetched (even if empty)
    var categoriesFetched by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        newMovementFormViewModel.fetchCategories(userId)
        // Small delay to ensure fetch completes
        kotlinx.coroutines.delay(500)
        categoriesFetched = true
    }

    LaunchedEffect(
        movementType,
        analyzedVoiceNote.suggestedCategory,
        categoriesFetched,
        formState.value.categories.size,
        suggestedCategoryProcessed
    ) {
        android.util.Log.d("EditVoiceNote", "LaunchedEffect triggered")
        android.util.Log.d("EditVoiceNote", "movementType: $movementType")
        android.util.Log.d("EditVoiceNote", "suggestedCategory: ${analyzedVoiceNote.suggestedCategory}")
        android.util.Log.d("EditVoiceNote", "categories count: ${formState.value.categories.size}")
        android.util.Log.d("EditVoiceNote", "categoriesFetched: $categoriesFetched")
        android.util.Log.d("EditVoiceNote", "selectedCategory: $selectedCategory")
        android.util.Log.d("EditVoiceNote", "suggestedCategoryProcessed: $suggestedCategoryProcessed")

        // Wait until categories fetch attempt is complete
        if (!categoriesFetched) {
            android.util.Log.d("EditVoiceNote", "Categories fetch not completed yet, waiting...")
            return@LaunchedEffect
        }

        // Only process once unless categories list changes
        if (suggestedCategoryProcessed && selectedCategory != null) {
            android.util.Log.d("EditVoiceNote", "Category already processed and selected, skipping...")
            return@LaunchedEffect
        }

        if (movementType == "expense" &&
            !analyzedVoiceNote.suggestedCategory.isNullOrBlank()
        ) {
            val suggested = analyzedVoiceNote.suggestedCategory.trim()
            android.util.Log.d("EditVoiceNote", "Searching for suggested category: '$suggested'")

            // Check if category exists (only if there are categories)
            val existingCategory = if (formState.value.categories.isNotEmpty()) {
                formState.value.categories.find {
                    it.name.trim().equals(suggested, ignoreCase = true)
                }
            } else {
                null
            }

            if (existingCategory != null) {
                android.util.Log.d("EditVoiceNote", "Found existing category: ${existingCategory.name}")
                selectedCategory = existingCategory
                suggestedCategoryProcessed = true
            } else if (!suggestedCategoryProcessed || selectedCategory == null) {
                android.util.Log.d("EditVoiceNote", "Category not found, showing dialog to create: '$suggested'")
                android.util.Log.d("EditVoiceNote", "Available categories: ${formState.value.categories.map { it.name }}")
                newCategoryName = suggested
                showNewCategoryDialog = true
            }
        }
    }

    if (showNewCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showNewCategoryDialog = false },
            title = { Text(stringResource(R.string.create_new_category)) },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text(stringResource(R.string.category_name)) },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        newCategoryFormViewModel.onNameChange(newCategoryName)
                        newCategoryFormViewModel.onDescriptionChange("Category automatically detected by AI")
                        newCategoryFormViewModel.createCategory(userId) {
                            showNewCategoryDialog = false
                            suggestedCategoryProcessed = true

                            coroutineScope.launch {
                                kotlinx.coroutines.delay(300)
                                newMovementFormViewModel.fetchCategories(userId)

                                kotlinx.coroutines.delay(200)
                                val newCategory = formState.value.categories.find {
                                    it.name.equals(newCategoryName, ignoreCase = true)
                                }
                                selectedCategory = newCategory
                            }
                        }
                    }
                ) {
                    Text(stringResource(R.string.create))
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewCategoryDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.review_voice_note_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (analyzedVoiceNote.confidence > 0) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.ai_confidence),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${(analyzedVoiceNote.confidence * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (analyzedVoiceNote.transcription.isNotBlank()) {
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(R.string.transcription),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = { showTranscription = !showTranscription }) {
                                Text(
                                    if (showTranscription) stringResource(R.string.hide)
                                    else stringResource(R.string.show)
                                )
                            }
                        }
                        if (showTranscription) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                analyzedVoiceNote.transcription,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Text(
                stringResource(R.string.movement_type),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = movementType == "expense",
                    onClick = { movementType = "expense" },
                    label = { Text(stringResource(R.string.expense)) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = movementType == "income",
                    onClick = {
                        movementType = "income"
                        selectedCategory = null
                    },
                    label = { Text(stringResource(R.string.income)) },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text(stringResource(R.string.amount)) },
                leadingIcon = {
                    Text(
                        CurrencyUtils.getCurrencySymbol(analyzedVoiceNote.currency),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.description)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            if (movementType == "expense") {
                Text(
                    stringResource(R.string.category),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = !expandedCategory }
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.select_category)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        formState.value.categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategory = category
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    newMovementFormViewModel.onNameChange(description)
                    newMovementFormViewModel.onDescriptionChange(analyzedVoiceNote.transcription)
                    newMovementFormViewModel.onAmountChange(amount)
                    newMovementFormViewModel.onDateTimeChange(analyzedVoiceNote.date)

                    if (movementType == "expense") {
                        newMovementFormViewModel.onMovementTypeChange(MovementType.EXPENSE)
                        selectedCategory?.let { newMovementFormViewModel.onCategorySelected(it) }

                        newMovementFormViewModel.onSourceTypeChange(com.grupo03.solea.data.models.SourceType.ITEM)
                        newMovementFormViewModel.onItemNameChange(description)
                        newMovementFormViewModel.onItemQuantityChange("1")
                        newMovementFormViewModel.onItemUnitPriceChange(amount)
                    } else {
                        newMovementFormViewModel.onMovementTypeChange(MovementType.INCOME)
                    }

                    newMovementFormViewModel.createMovement(userId) {
                        audioAnalysisViewModel.clearState()
                        onSuccess()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = amount.toDoubleOrNull() != null &&
                        description.isNotBlank() &&
                        (movementType == "income" || selectedCategory != null)
            ) {
                Text(stringResource(R.string.save_movement))
            }
        }
    }
}
