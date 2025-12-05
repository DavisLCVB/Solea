package com.grupo03.solea.ui.screens.forms

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo03.solea.R
import com.grupo03.solea.data.models.Category
import com.grupo03.solea.data.models.MovementType
import com.grupo03.solea.data.models.SourceType
import com.grupo03.solea.presentation.viewmodels.screens.NewMovementFormViewModel
import com.grupo03.solea.presentation.viewmodels.shared.AuthViewModel
import com.grupo03.solea.ui.theme.SoleaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMovementFormScreen(
    newMovementFormViewModel: NewMovementFormViewModel,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToNewCategory: () -> Unit
) {
    val formState = newMovementFormViewModel.formState.collectAsState()
    val userId = authViewModel.authState.collectAsState().value.user!!.uid
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Fetch categories on start
    LaunchedEffect(Unit) {
        newMovementFormViewModel.fetchCategories(userId)
    }

    // Show error message
    LaunchedEffect(formState.value.error) {
        formState.value.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = context.getString(error.messageRes)
            )
        }
    }

    // Show success message
    LaunchedEffect(formState.value.successMessage) {
        formState.value.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.new_movement)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                text = stringResource(R.string.register_a_new_income_or_expense),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Movement Type Selector - Disabled if pre-filled from shopping item
            val isFromShoppingItem = formState.value.name.isNotBlank() &&
                    formState.value.movementType == MovementType.EXPENSE &&
                    formState.value.itemName == formState.value.name

            Text(
                text = stringResource(R.string.movement_type),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                SegmentedButton(
                    selected = formState.value.movementType == MovementType.INCOME,
                    onClick = {
                        if (!isFromShoppingItem) {
                            newMovementFormViewModel.onMovementTypeChange(MovementType.INCOME)
                            newMovementFormViewModel.onCategorySelected(null)
                        }
                    },
                    enabled = !isFromShoppingItem,
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text(stringResource(R.string.income))
                }
                SegmentedButton(
                    selected = formState.value.movementType == MovementType.EXPENSE,
                    onClick = { newMovementFormViewModel.onMovementTypeChange(MovementType.EXPENSE) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text(stringResource(R.string.expense))
                }
            }

            // Name field
            OutlinedTextField(
                value = formState.value.name,
                onValueChange = newMovementFormViewModel::onNameChange,
                label = { Text(stringResource(R.string.movement_name)) },
                placeholder = { Text(stringResource(R.string.name_example)) },
                supportingText = {
                    if (!formState.value.isNameValid) {
                        Text(
                            text = stringResource(R.string.name_required_error),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                isError = !formState.value.isNameValid,
                enabled = !formState.value.isLoading,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Amount field
            OutlinedTextField(
                value = formState.value.amount,
                onValueChange = newMovementFormViewModel::onAmountChange,
                label = { Text(stringResource(R.string.amount_label)) },
                placeholder = { Text(stringResource(R.string.amount_placeholder)) },
                supportingText = {
                    if (!formState.value.isAmountValid) {
                        Text(
                            text = stringResource(R.string.enter_valid_amount_error),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                isError = !formState.value.isAmountValid,
                enabled = !formState.value.isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                prefix = { Text(formState.value.currency + " ") }
            )

            // Category selector with option to create new - only for expenses
            if (formState.value.movementType == MovementType.EXPENSE) {
                var expandedCategory by remember { mutableStateOf(false) }

                Text(
                    text = stringResource(R.string.category),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = !expandedCategory }
                ) {
                    OutlinedTextField(
                        value = formState.value.selectedCategory?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.select_category_label)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        isError = !formState.value.isCategorySelected,
                        supportingText = {
                            if (!formState.value.isCategorySelected) {
                                Text(
                                    text = stringResource(R.string.must_select_category_error),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                        enabled = !formState.value.isLoading
                    )

                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        // Option to create new category
                        DropdownMenuItem(
                            text = {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Text(stringResource(R.string.create_new_category_option))
                                }
                            },
                            onClick = {
                                expandedCategory = false
                                onNavigateToNewCategory()
                            }
                        )

                        // Existing categories
                        formState.value.categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    newMovementFormViewModel.onCategorySelected(category)
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }
            }

            // Description field
            OutlinedTextField(
                value = formState.value.description,
                onValueChange = newMovementFormViewModel::onDescriptionChange,
                label = { Text(stringResource(R.string.description_optional_label)) },
                placeholder = { Text(stringResource(R.string.add_description_placeholder)) },
                enabled = !formState.value.isLoading,
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            // Source fields - only for expenses
            if (formState.value.movementType == MovementType.EXPENSE) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.expense_details_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Source type selector
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = formState.value.sourceType == SourceType.ITEM,
                        onClick = { newMovementFormViewModel.onSourceTypeChange(SourceType.ITEM) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text(stringResource(R.string.single_item_label))
                    }
                    SegmentedButton(
                        selected = formState.value.sourceType == SourceType.RECEIPT,
                        onClick = { newMovementFormViewModel.onSourceTypeChange(SourceType.RECEIPT) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text(stringResource(R.string.receipt_invoice_label))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Show fields based on source type
                when (formState.value.sourceType) {
                    SourceType.ITEM -> {
                        // Item name
                        OutlinedTextField(
                            value = formState.value.itemName,
                            onValueChange = newMovementFormViewModel::onItemNameChange,
                            label = { Text(stringResource(R.string.item_name_label)) },
                            placeholder = { Text(stringResource(R.string.item_name_placeholder)) },
                            supportingText = {
                                if (!formState.value.isSourceValid && formState.value.itemName.isBlank()) {
                                    Text(
                                        text = stringResource(R.string.name_required_error),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            isError = !formState.value.isSourceValid && formState.value.itemName.isBlank(),
                            enabled = !formState.value.isLoading,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Quantity
                        OutlinedTextField(
                            value = formState.value.itemQuantity,
                            onValueChange = newMovementFormViewModel::onItemQuantityChange,
                            label = { Text(stringResource(R.string.quantity_label)) },
                            placeholder = { Text(stringResource(R.string.quantity_placeholder)) },
                            supportingText = {
                                if (!formState.value.isSourceValid && formState.value.itemQuantity.toDoubleOrNull() == null) {
                                    Text(
                                        text = stringResource(R.string.enter_valid_quantity_error),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            isError = !formState.value.isSourceValid && formState.value.itemQuantity.toDoubleOrNull() == null,
                            enabled = !formState.value.isLoading,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Unit price
                        OutlinedTextField(
                            value = formState.value.itemUnitPrice,
                            onValueChange = newMovementFormViewModel::onItemUnitPriceChange,
                            label = { Text(stringResource(R.string.unit_price_label)) },
                            placeholder = { Text(stringResource(R.string.price_placeholder)) },
                            supportingText = {
                                if (!formState.value.isSourceValid && formState.value.itemUnitPrice.toDoubleOrNull() == null) {
                                    Text(
                                        text = stringResource(R.string.enter_valid_price_error),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            isError = !formState.value.isSourceValid && formState.value.itemUnitPrice.toDoubleOrNull() == null,
                            enabled = !formState.value.isLoading,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            prefix = { Text(formState.value.currency + " ") }
                        )
                    }

                    SourceType.RECEIPT -> {
                        // Receipt description
                        OutlinedTextField(
                            value = formState.value.receiptDescription,
                            onValueChange = newMovementFormViewModel::onReceiptDescriptionChange,
                            label = { Text(stringResource(R.string.receipt_description_optional)) },
                            placeholder = { Text(stringResource(R.string.receipt_description_placeholder)) },
                            enabled = !formState.value.isLoading,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Receipt items header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.items_label),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            TextButton(
                                onClick = { newMovementFormViewModel.addReceiptItem() }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.add_item_button))
                            }
                        }

                        // Receipt items list
                        formState.value.receiptItems.forEachIndexed { index, item ->
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
                                                newMovementFormViewModel.removeReceiptItem(
                                                    index
                                                )
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = stringResource(R.string.delete_button)
                                            )
                                        }
                                    }

                                    OutlinedTextField(
                                        value = item.name,
                                        onValueChange = { newName ->
                                            newMovementFormViewModel.updateReceiptItem(
                                                index,
                                                item.copy(name = newName)
                                            )
                                        },
                                        label = { Text(stringResource(R.string.new_mov_name_label)) },
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
                                                newMovementFormViewModel.updateReceiptItem(
                                                    index,
                                                    item.copy(quantity = newQty)
                                                )
                                            },
                                            label = { Text(stringResource(R.string.quantity_label)) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )

                                        OutlinedTextField(
                                            value = item.unitPrice,
                                            onValueChange = { newPrice ->
                                                newMovementFormViewModel.updateReceiptItem(
                                                    index,
                                                    item.copy(unitPrice = newPrice)
                                                )
                                            },
                                            label = { Text(stringResource(R.string.price_label)) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            prefix = { Text(formState.value.currency + " ") }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cancel button
                OutlinedButton(
                    onClick = {
                        newMovementFormViewModel.clearForm()
                        onNavigateBack()
                    },
                    enabled = !formState.value.isLoading,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Text(
                        text = stringResource(R.string.cancel_button),
                        fontSize = 16.sp
                    )
                }

                // Submit button
                Button(
                    onClick = {
                        newMovementFormViewModel.createMovement(userId) {
                            newMovementFormViewModel.clearForm()
                            onNavigateBack()
                        }
                    },
                    enabled = !formState.value.isLoading &&
                            formState.value.isNameValid &&
                            formState.value.name.isNotBlank() &&
                            formState.value.isAmountValid &&
                            formState.value.amount.isNotBlank() &&
                            // Category is only required for expenses
                            (formState.value.movementType == MovementType.INCOME || formState.value.selectedCategory != null) &&
                            // Additional validation for expenses
                            (formState.value.movementType != MovementType.EXPENSE ||
                                    when (formState.value.sourceType) {
                                        SourceType.ITEM -> {
                                            formState.value.itemName.isNotBlank() &&
                                                    formState.value.itemQuantity.toDoubleOrNull() != null &&
                                                    formState.value.itemUnitPrice.toDoubleOrNull() != null
                                        }

                                        SourceType.RECEIPT -> {
                                            formState.value.receiptItems.isNotEmpty() &&
                                                    formState.value.receiptItems.all { item ->
                                                        item.name.isNotBlank() &&
                                                                item.quantity.toDoubleOrNull() != null &&
                                                                item.unitPrice.toDoubleOrNull() != null
                                                    }
                                        }
                                    }),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    if (formState.value.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Text(
                        text = if (formState.value.isLoading) stringResource(R.string.creating_button) else stringResource(R.string.create_button),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewMovementFormScreenPreview() {
    SoleaTheme(darkTheme = true) {
        NewMovementFormScreenContent(
            formState = com.grupo03.solea.presentation.states.screens.NewMovementFormState(
                description = "Almuerzo",
                amount = "25.50",
                selectedCategory = Category(id = "1", name = "Comida"),
                movementType = MovementType.EXPENSE,
                categories = listOf(
                    Category(id = "1", name = "Comida"),
                    Category(id = "2", name = "Transporte"),
                    Category(id = "3", name = "Entretenimiento")
                )
            ),
            onDescriptionChange = {},
            onAmountChange = {},
            onCategorySelected = {},
            onMovementTypeChange = {},
            onSubmit = {},
            onCancel = {},
            onCreateNewCategory = {},
            isLoading = false
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewMovementFormScreenContent(
    formState: com.grupo03.solea.presentation.states.screens.NewMovementFormState,
    onDescriptionChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onCategorySelected: (Category) -> Unit,
    onMovementTypeChange: (MovementType) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
    onCreateNewCategory: () -> Unit,
    isLoading: Boolean
) {
    var expandedCategory by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.register_a_new_income_or_expense),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.new_mvm_type_label),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            SegmentedButton(
                selected = formState.movementType == MovementType.INCOME,
                onClick = { onMovementTypeChange(MovementType.INCOME) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) {
                Text(stringResource(R.string.income_label))
            }
            SegmentedButton(
                selected = formState.movementType == MovementType.EXPENSE,
                onClick = { onMovementTypeChange(MovementType.EXPENSE) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) {
                Text(stringResource(R.string.expense_label))
            }
        }

        OutlinedTextField(
            value = formState.amount,
            onValueChange = onAmountChange,
            label = { Text(stringResource(R.string.amount)) },
            placeholder = { Text("0.00") },
            supportingText = {
                if (!formState.isAmountValid) {
                    Text(
                        text = stringResource(R.string.enter_a_new_amount),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            isError = !formState.isAmountValid,
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            prefix = { Text(formState.currency + " ") }
        )

        Text(
            text = "Categoría",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        ExposedDropdownMenuBox(
            expanded = expandedCategory,
            onExpandedChange = { expandedCategory = !expandedCategory }
        ) {
            OutlinedTextField(
                value = formState.selectedCategory?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.select_a_category)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                isError = !formState.isCategorySelected,
                supportingText = {
                    if (!formState.isCategorySelected) {
                        Text(
                            text = stringResource(R.string.must_select_a_category),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                enabled = !isLoading
            )

            ExposedDropdownMenu(
                expanded = expandedCategory,
                onDismissRequest = { expandedCategory = false }
            ) {
                DropdownMenuItem(
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text(stringResource(R.string.create_new_category))
                        }
                    },
                    onClick = {
                        expandedCategory = false
                        onCreateNewCategory()
                    }
                )

                formState.categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name) },
                        onClick = {
                            onCategorySelected(category)
                            expandedCategory = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = formState.description,
            onValueChange = onDescriptionChange,
            label = { Text(stringResource(R.string.optional_description)) },
            placeholder = { Text(stringResource(R.string.add_a_description)) },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                enabled = !isLoading,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Text(
                    text = "Cancelar",
                    fontSize = 16.sp
                )
            }

            Button(
                onClick = onSubmit,
                enabled = !isLoading &&
                        formState.isAmountValid &&
                        formState.amount.isNotBlank() &&
                        formState.selectedCategory != null,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text(
                    text = if (isLoading) stringResource(R.string.creating) else stringResource(R.string.create),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
