package com.grupo03.solea.data.repositories

import com.grupo03.solea.data.models.Movement
import com.grupo03.solea.data.models.MovementType

interface MovementsRepository {
    suspend fun createMovement(movement: Movement): String?;
    suspend fun createMovementType(type: MovementType): MovementType?;
    suspend fun getAllMovements(userId: String): List<Movement>;
    suspend fun getAllMovementTypes(movementTypeIds: List<String>): List<MovementType>;
    suspend fun updateMovement(movement: Movement): Boolean;
    suspend fun deleteMovement(movementId: String): Boolean;
}