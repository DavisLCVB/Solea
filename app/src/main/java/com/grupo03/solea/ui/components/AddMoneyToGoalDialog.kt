package com.grupo03.solea.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.unit.dp
import com.grupo03.solea.data.models.SavingsGoal
import com.grupo03.solea.utils.CurrencyUtils
import java.util.Locale

@Composable
fun AddMoneyToGoalDialog(
    goal: SavingsGoal,
    availableBalance: Double,
    userCurrency: String,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var isAmountValid by remember { mutableStateOf(true) }
    var isBalanceSufficient by remember { mutableStateOf(true) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val amountAsDouble = amount.toDoubleOrNull()
    val isAmountGreaterThanBalance = amountAsDouble != null && amountAsDouble > availableBalance
    val isAmountValidValue = amountAsDouble != null && amountAsDouble > 0
    val canConfirm = isAmountValidValue && !isAmountGreaterThanBalance

    val onAmountChange = { newAmount: String ->
        amount = newAmount
        val newAmountAsDouble = newAmount.toDoubleOrNull()
        isAmountValid = newAmount.isEmpty() || (newAmountAsDouble != null && newAmountAsDouble > 0)
        isBalanceSufficient = newAmount.isEmpty() || (newAmountAsDouble != null && newAmountAsDouble <= availableBalance)
    }

    val onConfirmClick = {
        if (canConfirm) {
            onConfirm(amountAsDouble!!)
            onDismiss()
        } else {
            if (!isAmountValidValue) {
                isAmountValid = false
            }
            if (isAmountGreaterThanBalance) {
                isBalanceSufficient = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar a \"${goal.name}\"") },
        text = {
            Column {
                Text("¿Cuánto quieres agregar a tu meta?")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Balance disponible: ${CurrencyUtils.getCurrencySymbol(userCurrency)} ${String.format(Locale.getDefault(), "%.2f", availableBalance)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    label = { Text("Monto") },
                    leadingIcon = { Text(CurrencyUtils.getCurrencySymbol(userCurrency)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                    modifier = Modifier.focusRequester(focusRequester),
                    isError = !isAmountValid || !isBalanceSufficient,
                    supportingText = {
                        when {
                            !isAmountValid && amount.isNotEmpty() -> {
                                Text(
                                    "Por favor, introduce un monto válido.",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            !isBalanceSufficient && amount.isNotEmpty() -> {
                                Text(
                                    "El monto no puede exceder el balance disponible.",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    },
                    singleLine = true
                )
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmClick,
                enabled = canConfirm
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
