package com.grupo03.solea.ui.screens.scanner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo03.solea.R
import com.grupo03.solea.data.models.Category
import com.grupo03.solea.data.models.EditableScannedItem
import com.grupo03.solea.presentation.viewmodels.screens.NewMovementFormViewModel
import com.grupo03.solea.presentation.viewmodels.screens.ScanReceiptViewModel
import com.grupo03.solea.presentation.viewmodels.shared.AuthViewModel
import com.grupo03.solea.utils.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScannedReceiptScreen(
    scanReceiptViewModel: ScanReceiptViewModel,
    newMovementFormViewModel: NewMovementFormViewModel,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val scanState = scanReceiptViewModel.state.collectAsState()
    val formState = newMovementFormViewModel.formState.collectAsState()
    val userId = authViewModel.authState.collectAsState().value.user!!.uid

    val scannedReceipt = scanState.value.scannedReceipt ?: return

    val userCurrency = CurrencyUtils.getCurrencyByCountry()
    val needsConversion = scannedReceipt.currency != userCurrency &&
                         CurrencyUtils.canConvert(scannedReceipt.currency, userCurrency)

    var editableItems by remember { mutableStateOf(scannedReceipt.items) }
    var establishmentName by remember { mutableStateOf(scannedReceipt.establishmentName) }
    var total by remember { mutableStateOf(scannedReceipt.total) }
    var currentCurrency by remember { mutableStateOf(scannedReceipt.currency) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var expandedCategory by remember { mutableStateOf(false) }
    var convertToUserCurrency by remember { mutableStateOf(needsConversion) }

    // Apply currency conversion when toggled
    LaunchedEffect(convertToUserCurrency) {
        if (convertToUserCurrency && needsConversion) {
            // Convert to user currency
            val convertedTotal = CurrencyUtils.convertCurrency(
                scannedReceipt.total.toDoubleOrNull() ?: 0.0,
                scannedReceipt.currency,
                userCurrency
            )
            total = String.format("%.2f", convertedTotal)
            currentCurrency = userCurrency

            editableItems = scannedReceipt.items.map { item ->
                val convertedPrice = CurrencyUtils.convertCurrency(
                    item.unitPrice.toDoubleOrNull() ?: 0.0,
                    scannedReceipt.currency,
                    userCurrency
                )
                item.copy(unitPrice = String.format("%.2f", convertedPrice))
            }
        } else {
            // Reset to original currency
            total = scannedReceipt.total
            currentCurrency = scannedReceipt.currency
            editableItems = scannedReceipt.items
        }
    }

    // Fetch categories
    LaunchedEffect(userId) {
        newMovementFormViewModel.fetchCategories(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.review_receipt_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.review_edit_detected_info),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Confidence indicator
            if (scannedReceipt.confidence > 0) {
                Text(
                    text = stringResource(R.string.confidence_format, (scannedReceipt.confidence * 100).toInt()),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Establishment name
            OutlinedTextField(
                value = establishmentName,
                onValueChange = { establishmentName = it },
                label = { Text(stringResource(R.string.establishment_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Total
            OutlinedTextField(
                value = total,
                onValueChange = { total = it },
                label = { Text(stringResource(R.string.total_label)) },
                prefix = { Text("${currentCurrency} ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Currency conversion toggle (only show if conversion is possible)
            if (needsConversion) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.convert_to_your_currency),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = stringResource(
                                R.string.convert_currency_format,
                                scannedReceipt.currency,
                                userCurrency
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = convertToUserCurrency,
                        onCheckedChange = { convertToUserCurrency = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Category selector
            ExposedDropdownMenuBox(
                expanded = expandedCategory,
                onExpandedChange = { expandedCategory = it }
            ) {
                OutlinedTextField(
                    value = selectedCategory?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.category_label)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    isError = selectedCategory == null
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

            Spacer(modifier = Modifier.height(8.dp))

            // Items header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.articles_label),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                TextButton(
                    onClick = {
                        editableItems = editableItems + EditableScannedItem()
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.add))
                }
            }

            // Items list
            editableItems.forEachIndexed { index, item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.item_number_format, index + 1),
                                fontWeight = FontWeight.Medium
                            )
                            IconButton(
                                onClick = {
                                    editableItems = editableItems.toMutableList().apply {
                                        removeAt(index)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                            }
                        }

                        OutlinedTextField(
                            value = item.description,
                            onValueChange = { newDesc ->
                                editableItems = editableItems.toMutableList().apply {
                                    this[index] = this[index].copy(description = newDesc)
                                }
                            },
                            label = { Text(stringResource(R.string.description_label)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = item.quantity,
                                onValueChange = { newQty ->
                                    editableItems = editableItems.toMutableList().apply {
                                        this[index] = this[index].copy(quantity = newQty)
                                    }
                                },
                                label = { Text(stringResource(R.string.quantity_label)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = item.unitPrice,
                                onValueChange = { newPrice ->
                                    editableItems = editableItems.toMutableList().apply {
                                        this[index] = this[index].copy(unitPrice = newPrice)
                                    }
                                },
                                label = { Text(stringResource(R.string.price_label)) },
                                prefix = { Text("${currentCurrency} ") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = item.category,
                            onValueChange = { newCat ->
                                editableItems = editableItems.toMutableList().apply {
                                    this[index] = this[index].copy(category = newCat)
                                }
                            },
                            label = { Text(stringResource(R.string.category_optional_label)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        scanReceiptViewModel.clearState()
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Text(stringResource(R.string.cancel_button), fontSize = 16.sp)
                }

                Button(
                    onClick = {
                        selectedCategory?.let { category ->
                            // Load scanned receipt data into form
                            newMovementFormViewModel.loadFromScannedReceipt(
                                establishmentName = establishmentName,
                                total = total,
                                currency = currentCurrency,
                                items = editableItems
                            )

                            // Set the category
                            newMovementFormViewModel.onCategorySelected(category)

                            // Create the movement
                            newMovementFormViewModel.createMovement(userId, onSuccess)
                        }
                    },
                    enabled = selectedCategory != null &&
                              editableItems.isNotEmpty() &&
                              editableItems.all {
                                  it.description.isNotBlank() &&
                                  it.quantity.toDoubleOrNull() != null &&
                                  it.unitPrice.toDoubleOrNull() != null
                              },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Text(
                        stringResource(R.string.create_movement_button),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
