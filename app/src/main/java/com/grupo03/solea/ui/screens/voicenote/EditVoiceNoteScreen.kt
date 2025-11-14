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

    val coroutineScope = rememberCoroutineScope()

    // Auto-select suggested category
    LaunchedEffect(analyzedVoiceNote.suggestedCategory, formState.value.categories) {
        if (selectedCategory == null && !analyzedVoiceNote.suggestedCategory.isNullOrBlank()) {
            val suggested = analyzedVoiceNote.suggestedCategory
            val existingCategory = formState.value.categories.find {
                it.name.equals(suggested, ignoreCase = true)
            }

            if (existingCategory != null) {
                selectedCategory = existingCategory
            } else {
                newCategoryName = suggested
                showNewCategoryDialog = true
            }
        }
    }

    LaunchedEffect(userId) {
        newMovementFormViewModel.fetchCategories(userId)
    }

    // Handle category creation dialog
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
                            // Close dialog immediately
                            showNewCategoryDialog = false

                            // Refresh categories and select the new one
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
            // AI Confidence indicator
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

            // Transcription (collapsible)
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

            // Movement Type selector
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
                    onClick = { movementType = "income" },
                    label = { Text(stringResource(R.string.income)) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Amount field
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

            // Description field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.description)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            // Category selector
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

            // Save button
            Button(
                onClick = {
                    selectedCategory?.let { category ->
                        // Set all form values
                        newMovementFormViewModel.onNameChange(description)
                        newMovementFormViewModel.onDescriptionChange(analyzedVoiceNote.transcription)
                        newMovementFormViewModel.onAmountChange(amount)
                        newMovementFormViewModel.onCategorySelected(category)
                        newMovementFormViewModel.onDateTimeChange(analyzedVoiceNote.date)

                        if (movementType == "expense") {
                            newMovementFormViewModel.onMovementTypeChange(MovementType.EXPENSE)

                            // For expenses, populate Item source fields
                            newMovementFormViewModel.onSourceTypeChange(com.grupo03.solea.data.models.SourceType.ITEM)
                            newMovementFormViewModel.onItemNameChange(description)
                            newMovementFormViewModel.onItemQuantityChange("1")
                            newMovementFormViewModel.onItemUnitPriceChange(amount)
                        } else {
                            newMovementFormViewModel.onMovementTypeChange(MovementType.INCOME)
                        }

                        // Create movement
                        newMovementFormViewModel.createMovement(userId) {
                            audioAnalysisViewModel.clearState()
                            onSuccess()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = amount.toDoubleOrNull() != null &&
                         description.isNotBlank() &&
                         selectedCategory != null
            ) {
                Text(stringResource(R.string.save_movement))
            }
        }
    }
}
