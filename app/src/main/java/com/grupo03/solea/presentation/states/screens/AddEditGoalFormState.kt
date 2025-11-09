package com.grupo03.solea.presentation.states.screens

import com.grupo03.solea.data.models.SavingsGoal
import com.grupo03.solea.utils.AppError
import java.time.Instant

/**
 * Represents the state of the Add/Edit Goal form.
 *
 * @param existingGoal The goal being edited, or null if creating a new one.
 * @param name The name of the goal.
 * @param targetAmount The target amount as a string for the text field.
 * @param deadline The selected deadline for the goal.
 * @param isNameValid Whether the current name is valid.
 * @param isAmountValid Whether the current amount is valid.
 * @param isLoading True if the form is currently saving/deleting data.
 * @param error Any error that occurred during the process.
 */
data class AddEditGoalFormState(
    val existingGoal: SavingsGoal? = null,
    val name: String = "",
    val targetAmount: String = "",
    val deadline: Instant = Instant.now(),
    val isNameValid: Boolean = true,
    val isAmountValid: Boolean = true,
    val isLoading: Boolean = false,
    val error: AppError? = null
)
