package com.grupo03.solea.ui.screens.savings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
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
import com.grupo03.solea.presentation.viewmodels.screens.SavingsViewModel
import com.grupo03.solea.presentation.viewmodels.shared.AuthViewModel
import com.grupo03.solea.utils.CurrencyUtils
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.grupo03.solea.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditGoalScreen(
    savingsViewModel: SavingsViewModel,
    authViewModel: AuthViewModel,
    onSaveSuccess: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formState by savingsViewModel.formState.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val user = authState.user
    val userCurrency = user?.currency ?: "USD"

    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val isEditMode = formState.existingGoal != null

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // --- Top Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                }
                Text(
                    text = if (isEditMode) stringResource(R.string.edit) else stringResource(R.string.add),
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            if (isEditMode) {
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error)
                }
            }
        }

        HorizontalDivider()

        // --- Form Fields ---
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Name Field
            OutlinedTextField(
                value = formState.name,
                onValueChange = savingsViewModel::onNameChange,
                label = { Text(stringResource(R.string.goal_name)) },
                placeholder = { Text(stringResource(R.string.goal_mock_desc)) },
                isError = !formState.isNameValid,
                supportingText = {
                    if (!formState.isNameValid) Text(stringResource(R.string.goal_name_adv))
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Target Amount Field
            OutlinedTextField(
                value = formState.targetAmount,
                onValueChange = savingsViewModel::onAmountChange,
                label = { Text(stringResource(R.string.goal_amount)) },
                leadingIcon = { Text(CurrencyUtils.getCurrencySymbol(userCurrency)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = !formState.isAmountValid,
                supportingText = {
                    if (!formState.isAmountValid) Text(stringResource(R.string.goal_amount_adv))
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Deadline Picker
            OutlinedCard(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                val formatter = remember { DateTimeFormatter.ofPattern("dd MMMM, yyyy") }
                val formattedDate = remember(formState.deadline) {
                    formState.deadline.atZone(ZoneId.systemDefault()).toLocalDate().format(formatter)
                }
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(stringResource(R.string.goal_deadline), style = MaterialTheme.typography.labelMedium)
                        Text(formattedDate, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    }
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- Action Buttons ---
            Button(
                onClick = { user?.uid?.let { savingsViewModel.saveGoal(it, onSaveSuccess) } },
                enabled = formState.name.isNotBlank() && formState.targetAmount.isNotBlank() && formState.isAmountValid && !formState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(if (isEditMode) stringResource(R.string.update) else stringResource(R.string.add_new))
            }
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    }

    // --- Dialogs ---
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = formState.deadline.toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        savingsViewModel.onDeadlineChange(Instant.ofEpochMilli(it))
                    }
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel)) } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.goal_delete_title)) },
            text = { Text(stringResource(R.string.goal_delete_adv)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        savingsViewModel.deleteGoal(onSaveSuccess)
                        showDeleteDialog = false
                    }
                ) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}
