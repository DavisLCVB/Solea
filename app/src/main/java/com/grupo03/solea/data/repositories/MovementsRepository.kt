package com.grupo03.solea.data.repositories

import com.grupo03.solea.data.models.Movement
import com.grupo03.solea.data.models.MovementType

interface MovementsRepository {
    suspend fun createMovement(movement: Movement): String?;
    suspend fun createMovementType(type: MovementType): MovementType?;
    suspend fun getAllMovements(): List<Movement>?;
}