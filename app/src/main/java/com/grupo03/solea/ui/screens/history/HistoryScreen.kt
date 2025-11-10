package com.grupo03.solea.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo03.solea.R
import com.grupo03.solea.data.models.Category
import com.grupo03.solea.presentation.states.screens.DateFilter
import com.grupo03.solea.presentation.states.screens.HistoryMovementItem
import com.grupo03.solea.presentation.viewmodels.screens.HistoryViewModel
import com.grupo03.solea.presentation.viewmodels.shared.AuthViewModel
import com.grupo03.solea.ui.components.MovementCard
import com.grupo03.solea.ui.components.MovementDetailsModal
import com.grupo03.solea.ui.components.SectionTitle
import com.grupo03.solea.ui.components.TopBar
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun HistoryScreen(
    historyViewModel: HistoryViewModel,
    authViewModel: AuthViewModel,
    movementsViewModel: com.grupo03.solea.presentation.viewmodels.shared.MovementsViewModel,
    modifier: Modifier = Modifier
) {
    val historyState = historyViewModel.historyState.collectAsState()
    val movementsState = movementsViewModel.movementsState.collectAsState()
    val authState = authViewModel.authState.collectAsState()
    val userId = authState.value.user?.uid ?: ""

    // Observe movements in real-time
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            movementsViewModel.observeMovements(userId)
            movementsViewModel.observeCategories(userId)
        }
    }

    // Update historyViewModel when movementsState changes
    LaunchedEffect(
        movementsState.value.incomeDetailsList,
        movementsState.value.expenseDetailsList,
        movementsState.value.saveDetailsList
    ) {
        historyViewModel.updateMovements(
            movementsState.value.incomeDetailsList,
            movementsState.value.expenseDetailsList,
            movementsState.value.saveDetailsList
        )
    }

    // Re-apply filter when selected filter changes
    LaunchedEffect(historyState.value.selectedFilter) {
        historyViewModel.updateMovements(
            movementsState.value.incomeDetailsList,
            movementsState.value.expenseDetailsList,
            movementsState.value.saveDetailsList
        )
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        TopBar(title = "Historial")

        Column(
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            // Filtros y fecha
            HistoryFilters(
                selectedFilter = historyState.value.selectedFilter,
                dateRangeText = historyViewModel.getDateRangeText(),
                onFilterSelected = { filter -> historyViewModel.onFilterSelected(filter) },
                onCustomDateRangeSelected = { startDate, endDate ->
                    historyViewModel.onCustomDateRangeSelected(startDate, endDate)
                }
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Loading state
            if (historyState.value.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            // Error state
            else if (historyState.value.error != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    var text = "Unknown error"
                    if (historyState.value.error != null) {
                        text = stringResource(historyState.value.error!!.messageRes)
                    }
                    Text(
                        text = text,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            // Content
            else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    if (historyState.value.groupedMovements.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay transacciones",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            historyState.value.groupedMovements.forEach { group ->
                                item {
                                    Text(
                                        text = group.label,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(
                                            top = 8.dp,
                                            bottom = 4.dp,
                                            start = 8.dp
                                        )
                                    )
                                }
                                items(group.movements) { movement ->
                                    Box(
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) {
                                        when (movement) {
                                            is HistoryMovementItem.IncomeItem -> {
                                                Card(
                                                    onClick = {
                                                        historyViewModel.onMovementSelected(
                                                            movement
                                                        )
                                                    },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                                                    )
                                                ) {
                                                    MovementCard(
                                                        incomeDetails = movement.incomeDetails,
                                                        expenseDetails = null,
                                                        saveDetails = null,
                                                        category = Category()
                                                    )
                                                }
                                            }

                                            is HistoryMovementItem.ExpenseItem -> {
                                                Card(
                                                    onClick = {
                                                        historyViewModel.onMovementSelected(
                                                            movement
                                                        )
                                                    },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                                                    )
                                                ) {
                                                    MovementCard(
                                                        incomeDetails = null,
                                                        expenseDetails = movement.expenseDetails,
                                                        saveDetails = null,
                                                        category = Category()
                                                    )
                                                }
                                            }

                                            is HistoryMovementItem.SaveItem -> {
                                                Card(
                                                    onClick = {
                                                        historyViewModel.onMovementSelected(
                                                            movement
                                                        )
                                                    },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                                                    )
                                                ) {
                                                    MovementCard(
                                                        incomeDetails = null,
                                                        expenseDetails = null,
                                                        saveDetails = movement.saveDetails,
                                                        category = Category()
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Show details modal
            historyState.value.selectedMovement?.let { movement ->
                val movementId = when (movement) {
                    is HistoryMovementItem.IncomeItem -> movement.incomeDetails.movement.id
                    is HistoryMovementItem.ExpenseItem -> movement.expenseDetails.movement.id
                    is HistoryMovementItem.SaveItem -> movement.saveDetails.movement.id
                }

                MovementDetailsModal(
                    movement = movement,
                    onDismissRequest = { historyViewModel.onMovementSelected(null) },
                    onDelete = {
                        movementsViewModel.deleteMovement(
                            movementId = movementId,
                            onSuccess = {
                                historyViewModel.onMovementSelected(null)
                            },
                            onError = { error ->
                                // TODO: Show error to user
                                historyViewModel.onMovementSelected(null)
                            }
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryFilters(
    selectedFilter: DateFilter,
    dateRangeText: String,
    onFilterSelected: (DateFilter) -> Unit,
    onCustomDateRangeSelected: (LocalDate, LocalDate) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var isSelectingStartDate by remember { mutableStateOf(true) }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }

    SectionTitle(
        text = stringResource(R.string.history_transactions),
        icon = Icons.Default.Receipt,
        modifier = Modifier.padding(top = 10.dp)
    )
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val filters = listOf(
            stringResource(R.string.history_filter_today) to DateFilter.TODAY,
            stringResource(R.string.history_filter_week) to DateFilter.WEEK,
            stringResource(R.string.history_filter_month) to DateFilter.MONTH,
            stringResource(R.string.history_filter_year) to DateFilter.YEAR,
            stringResource(R.string.history_filter_pers) to DateFilter.CUSTOM
        )

        filters.forEach { (label, filter) ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = {
                    if (filter == DateFilter.CUSTOM) {
                        isSelectingStartDate = true
                        startDate = null
                        endDate = null
                        showDatePicker = true
                    } else {
                        onFilterSelected(filter)
                    }
                },
                label = { Text(label, fontSize = 12.sp) },
                modifier = Modifier.height(28.dp)
            )
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialDisplayMode = DisplayMode.Picker
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()

                            if (isSelectingStartDate) {
                                startDate = selectedDate
                                isSelectingStartDate = false
                                datePickerState.selectedDateMillis = null
                            } else {
                                endDate = selectedDate
                                if (startDate != null && endDate != null) {
                                    onCustomDateRangeSelected(startDate!!, endDate!!)
                                    showDatePicker = false
                                }
                            }
                        }
                    }
                ) {
                    Text(
                        if (isSelectingStartDate) stringResource(R.string.picker_next) else stringResource(
                            R.string.picker_confirm
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.history_filters_cancel))
                }
            }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isSelectingStartDate) stringResource(R.string.history_filter_select_initial_date) else stringResource(
                        R.string.history_filter_select_end_date
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                DatePicker(state = datePickerState)
            }
        }
    }
}

