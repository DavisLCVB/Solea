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
            Log.d(TAG, "createGoal: Starting goal creation for userId=${goal.userId}")
            val doc = firestore.collection(COLLECTION_NAME).document()
            val goalWithId = goal.copy(id = doc.id)
            doc.set(goalWithId.toMap()).await()
            Log.d(TAG, "createGoal: Goal created successfully with ID: ${doc.id}")
            RepositoryResult.Success(goalWithId)
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "createGoal: FirebaseFirestoreException", e)
            RepositoryResult.Error(mapFirestoreException(e, SavingsGoalError.CREATION_FAILED))
        } catch (e: Exception) {
            Log.e(TAG, "createGoal: Exception", e)
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
            Log.e(TAG, "getGoalById: FirebaseFirestoreException", e)
            RepositoryResult.Error(mapFirestoreException(e, SavingsGoalError.FETCH_FAILED))
        } catch (e: Exception) {
            Log.e(TAG, "getGoalById: Exception", e)
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
            Log.e(TAG, "getGoalsByUser: FirebaseFirestoreException", e)
            RepositoryResult.Error(mapFirestoreException(e, SavingsGoalError.FETCH_FAILED))
        } catch (e: Exception) {
            Log.e(TAG, "getGoalsByUser: Exception", e)
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
            Log.e(TAG, "updateGoal: FirebaseFirestoreException", e)
            RepositoryResult.Error(mapFirestoreException(e, SavingsGoalError.UPDATE_FAILED))
        } catch (e: Exception) {
            Log.e(TAG, "updateGoal: Exception", e)
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
            Log.e(TAG, "deleteGoal: FirebaseFirestoreException", e)
            RepositoryResult.Error(mapFirestoreException(e, SavingsGoalError.DELETE_FAILED))
        } catch (e: Exception) {
            Log.e(TAG, "deleteGoal: Exception", e)
            RepositoryResult.Error(SavingsGoalError.UNKNOWN_ERROR)
        }
    }

    override suspend fun updateCurrentAmount(goalId: String, amount: Double): RepositoryResult<SavingsGoal> {
        return try {
            val goalResult = getGoalById(goalId)
            if (goalResult is RepositoryResult.Error) {
                return goalResult
            }

            val goal = (goalResult as RepositoryResult.Success).data ?: return RepositoryResult.Error(SavingsGoalError.NOT_FOUND)
            val updatedGoal = goal.copy(
                currentAmount = amount,
                isCompleted = amount >= goal.targetAmount
            )

            updateGoal(updatedGoal)
        } catch (e: Exception) {
            Log.e(TAG, "updateCurrentAmount: Exception", e)
            RepositoryResult.Error(SavingsGoalError.UPDATE_FAILED)
        }
    }

    override fun observeGoalsByUser(userId: String): Flow<RepositoryResult<List<SavingsGoal>>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_NAME)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "observeGoalsByUser: FirebaseFirestoreException", error)
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