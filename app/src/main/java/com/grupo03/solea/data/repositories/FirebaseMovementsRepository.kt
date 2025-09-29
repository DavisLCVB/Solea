package com.grupo03.solea.data.repositories

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.grupo03.solea.data.models.Movement
import com.grupo03.solea.data.models.MovementType
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
            doc.set(movementWithId.toMap()).await()
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
            doc.set(typeWithId.toMap()).await()
            typeWithId
        } catch (e: Exception) {
            Log.d(TAG, "createMovementType: ${e.message}", e)
            null
        }
    }

    override suspend fun getAllMovements(userId: String): List<Movement> {
        return try {
            val querySnapshot = firestore
                .collection(DatabaseContants.MOVEMENTS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val movements = querySnapshot.documents.mapNotNull { document ->
                val data = document.data
                if (data != null) {
                    Movement.fromMap(data)
                } else {
                    null
                }
            }
            movements
        } catch (e: Exception) {
            Log.d(TAG, "getAllMovements: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getAllMovementTypes(movementTypeIds: List<String>): List<MovementType> {
        return try {
            if (movementTypeIds.isEmpty()) return emptyList()
            val querySnapshot = firestore
                .collection(DatabaseContants.MOVEMENT_TYPES_COLLECTION)
                .whereIn("id", movementTypeIds)
                .get()
                .await()
            val types = querySnapshot.documents.mapNotNull { document ->
                val data = document.data
                if (data != null) {
                    MovementType.fromMap(data)
                } else {
                    null
                }
            }
            types
        } catch (e: Exception) {
            Log.d(TAG, "getAllMovementTypes: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun updateMovement(movement: Movement): Boolean {
        return try {
            val docRef = firestore
                .collection(DatabaseContants.MOVEMENTS_COLLECTION)
                .document(movement.id)
            docRef.set(movement.toMap()).await()
            true
        } catch (e: Exception) {
            Log.d(TAG, "updateMovement: ${e.message}", e)
            false
        }
    }

    override suspend fun deleteMovement(movementId: String): Boolean {
        return try {
            val docRef = firestore
                .collection(DatabaseContants.MOVEMENTS_COLLECTION)
                .document(movementId)
            docRef.delete().await()
            true
        } catch (e: Exception) {
            Log.d(TAG, "deleteMovement: ${e.message}", e)
            false
        }
    }

    override suspend fun getAllMovementTypesByUser(userId: String): List<MovementType> {
        return try {
            val querySnapshot = firestore
                .collection(DatabaseContants.MOVEMENT_TYPES_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val types = querySnapshot.documents.mapNotNull { document ->
                val data = document.data
                if (data != null) {
                    MovementType.fromMap(data)
                } else {
                    null
                }
            }
            types
        } catch (e: Exception) {
            Log.d(TAG, "getAllMovementTypesByUser: ${e.message}", e)
            emptyList()
        }
    }
}