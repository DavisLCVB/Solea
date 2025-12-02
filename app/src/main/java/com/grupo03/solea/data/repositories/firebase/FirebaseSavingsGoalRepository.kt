package com.grupo03.solea.data.repositories.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.grupo03.solea.data.models.SavingsGoal
import com.grupo03.solea.data.repositories.interfaces.SavingsGoalRepository
import com.grupo03.solea.utils.RepositoryResult
import com.grupo03.solea.utils.SavingsGoalError
import com.grupo03.solea.utils.mapFirestoreException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseSavingsGoalRepository(
    private val firestore: FirebaseFirestore
) : SavingsGoalRepository {

    companion object {
        private const val TAG = "FirebaseSavingsGoalRepo"
        private const val COLLECTION_NAME = "savingsGoals"
    }

    override suspend fun createGoal(goal: SavingsGoal): RepositoryResult<SavingsGoal> {
        return try {
            val doc = firestore.collection(COLLECTION_NAME).document()
            val goalWithId = goal.copy(id = doc.id)
            doc.set(goalWithId.toMap()).await()
            RepositoryResult.Success(goalWithId)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, SavingsGoalError.CREATION_FAILED))
        } catch (e: Exception) {
            RepositoryResult.Error(SavingsGoalError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getGoalById(goalId: String): RepositoryResult<SavingsGoal?> {
        return try {
            val docSnapshot = firestore.collection(COLLECTION_NAME)
                .document(goalId)
                .get()
                .await()
            val goal = SavingsGoal.fromMap(docSnapshot.data ?: emptyMap())
            RepositoryResult.Success(goal)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, SavingsGoalError.FETCH_FAILED))
        } catch (e: Exception) {
            RepositoryResult.Error(SavingsGoalError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getGoalsByUser(userId: String): RepositoryResult<List<SavingsGoal>> {
        return try {
            val querySnapshot = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val goals = querySnapshot.documents.mapNotNull { document ->
                SavingsGoal.fromMap(document.data ?: emptyMap())
            }
            RepositoryResult.Success(goals)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, SavingsGoalError.FETCH_FAILED))
        } catch (e: Exception) {
            RepositoryResult.Error(SavingsGoalError.UNKNOWN_ERROR)
        }
    }

    override suspend fun updateGoal(goal: SavingsGoal): RepositoryResult<SavingsGoal> {
        return try {
            firestore.collection(COLLECTION_NAME)
                .document(goal.id)
                .set(goal.toMap())
                .await()
            RepositoryResult.Success(goal)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, SavingsGoalError.UPDATE_FAILED))
        } catch (e: Exception) {
            RepositoryResult.Error(SavingsGoalError.UNKNOWN_ERROR)
        }
    }

    override suspend fun deleteGoal(goalId: String): RepositoryResult<Unit> {
        return try {
            firestore.collection(COLLECTION_NAME)
                .document(goalId)
                .delete()
                .await()
            RepositoryResult.Success(Unit)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, SavingsGoalError.DELETE_FAILED))
        } catch (e: Exception) {
            RepositoryResult.Error(SavingsGoalError.UNKNOWN_ERROR)
        }
    }

    override suspend fun updateCurrentAmount(goalId: String, amountToAdd: Double): RepositoryResult<SavingsGoal> {
        return try {
            val goalRef = firestore.collection(COLLECTION_NAME).document(goalId)
            var finalGoal: SavingsGoal? = null

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(goalRef)
                val goal = SavingsGoal.fromMap(snapshot.data ?: emptyMap())
                    ?: throw FirebaseFirestoreException("Goal not found", FirebaseFirestoreException.Code.NOT_FOUND)

                val newAmount = goal.currentAmount + amountToAdd
                val updatedGoal = goal.copy(
                    currentAmount = newAmount,
                    isCompleted = newAmount >= goal.targetAmount
                )
                transaction.set(goalRef, updatedGoal.toMap())
                finalGoal = updatedGoal
            }.await()

            finalGoal?.let {
                RepositoryResult.Success(it)
            } ?: RepositoryResult.Error(SavingsGoalError.UPDATE_FAILED)

        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, SavingsGoalError.UPDATE_FAILED))
        } catch (e: Exception) {
            RepositoryResult.Error(SavingsGoalError.UNKNOWN_ERROR)
        }
    }

    override fun observeGoalsByUser(userId: String): Flow<RepositoryResult<List<SavingsGoal>>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_NAME)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(RepositoryResult.Error(mapFirestoreException(error, SavingsGoalError.FETCH_FAILED)))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val goals = snapshot.documents.mapNotNull { document ->
                        SavingsGoal.fromMap(document.data ?: emptyMap())
                    }
                    trySend(RepositoryResult.Success(goals))
                }
            }

        awaitClose { listener.remove() }
    }
}