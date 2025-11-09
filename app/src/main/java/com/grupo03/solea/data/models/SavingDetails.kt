package com.grupo03.solea.data.models

/**
 * Data class to encapsulate the details of a saving movement.
 *
 * @param movement The core movement object of type SAVING.
 */
data class SavingDetails(
    val movement: Movement = Movement(type = MovementType.SAVING)
)