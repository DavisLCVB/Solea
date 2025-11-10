package com.grupo03.solea.data.models

import com.grupo03.solea.data.models.Movement

/**
 * Represents a saving action, which is a type of movement.
 *
 * @property id Unique identifier for the saving
 * @property movementId ID of the associated Movement (must be of type SAVING)
 * @property goalId ID del objetivo/chanchito
 * @property amount Amount saved
 */
data class Save(
    val id: String = "",
    val movementId: String = "",
    val goalId: String = "", // ID del objetivo/chanchito
    val amount: Double = 0.0
)

/**
 * Wrapper class that holds a Save object and its associated Movement details.
 * This follows the same pattern as IncomeDetails and ExpenseDetails.
 */
data class SaveDetails(
    val save: Save = Save(),
    val movement: Movement = Movement()
)
