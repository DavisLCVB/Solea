package com.grupo03.solea.data.repositories

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.grupo03.solea.data.models.Movement
import com.grupo03.solea.data.models.MovementType
import com.grupo03.solea.data.models.toMap
import com.grupo03.solea.utils.DatabaseContants
import kotlinx.coroutines.tasks.await

class FirebaseMovementsRepository(
    private val firestore: FirebaseFirestore,
) : MovementsRepository {
    companion object {
        private const val TAG = "FirebaseMovementsRepo"
    }

    override suspend fun createMovement(movement: Movement): String? {
        val doc = firestore
            .collection(DatabaseContants.MOVEMENTS_COLLECTION)
            .document();
        val movementWithId = movement.copy(id = doc.id)
        return try {
            doc.set(toMap(movementWithId)).await()
            doc.id
        } catch (e: Exception) {
            Log.d(TAG, "createMovement: ${e.message}", e)
            null
        }
    }

    override suspend fun createMovementType(type: MovementType): MovementType? {
        val doc = firestore
            .collection(DatabaseContants.MOVEMENT_TYPES_COLLECTION)
            .document();
        val typeWithId = type.copy(id = doc.id)
        return try {
            doc.set(toMap(typeWithId)).await()
            typeWithId
        } catch (e: Exception) {
            Log.d(TAG, "createMovementType: ${e.message}", e)
            null
        }
    }

    override suspend fun getAllMovements(): List<Movement>? {
        TODO("Not yet implemented")
    }
}