package com.grupo03.solea.data.models

/**
 * Represents an income transaction.
 *
 * An Income is a specific type of Movement that tracks financial inflows.
 * Unlike Expenses, Incomes don't have complex source tracking and are simpler in structure.
 *
 * @property id Unique identifier for the income
 * @property movementId ID of the associated Movement (must be of type INCOME)
 */
data class Income(
    val id: String = "",
    val movementId: String = "",
)

/**
 * Complete income information with all related data.
 *
 * This aggregates an Income with its Movement, providing a complete view
 * of the income transaction.
 *
 * @property income The income record
 * @property movement The associated movement record
 */
data class IncomeDetails(
    val income: Income = Income(),
    val movement: Movement = Movement(),
)
