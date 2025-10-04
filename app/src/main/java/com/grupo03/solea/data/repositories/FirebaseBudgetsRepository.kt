package com.grupo03.solea.data.repositories

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.grupo03.solea.data.models.Budget
import com.grupo03.solea.data.models.Status
import com.grupo03.solea.utils.DatabaseContants
import kotlinx.coroutines.tasks.await

class FirebaseBudgetsRepository(
    private val firestore: FirebaseFirestore
) : BudgetsRepository {
    companion object {
        private const val TAG = "FirebaseBudgetsRepo"
    }

    override suspend fun createBudget(budget: Budget): Budget? {
        val doc = firestore
            .collection(DatabaseContants.BUDGETS_COLLECTION)
            .document()
        val budgetWithId = budget.copy(id = doc.id)
        return try {
            doc.set(budgetWithId.toMap()).await()
            budgetWithId
        } catch (e: Exception) {
            Log.d(TAG, "createBudget: ${e.message}", e)
            null
        }
    }

    override suspend fun getAllBudgetsByUser(userId: String): List<Budget> {
        return try {
            val querySnapshot = firestore
                .collection(DatabaseContants.BUDGETS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            querySnapshot.documents.mapNotNull { document ->
                document.data?.let { Budget.fromMap(it) }
            }
        } catch (e: Exception) {
            Log.d(TAG, "getAllBudgetsByUser: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun updateBudget(budget: Budget): Boolean {
        return try {
            val docRef = firestore
                .collection(DatabaseContants.BUDGETS_COLLECTION)
                .document(budget.id)
            docRef.set(budget.toMap()).await()
            true
        } catch (e: Exception) {
            Log.d(TAG, "updateBudget: ${e.message}", e)
            false
        }
    }

    override suspend fun deleteBudget(budgetId: String): Boolean {
        return try {
            val docRef = firestore
                .collection(DatabaseContants.BUDGETS_COLLECTION)
                .document(budgetId)
            docRef.delete().await()
            true
        } catch (e: Exception) {
            Log.d(TAG, "deleteBudget: ${e.message}", e)
            false
        }
    }

    override suspend fun getBudgetByMovementType(userId: String, movementTypeId: String): Budget? {
        return try {
            val querySnapshot = firestore
                .collection(DatabaseContants.BUDGETS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("movementTypeId", movementTypeId)
                .get()
                .await()
            querySnapshot.documents.firstOrNull()?.data?.let { Budget.fromMap(it) }
        } catch (e: Exception) {
            Log.d(TAG, "getBudgetByMovementType: ${e.message}", e)
            null
        }
    }

    override suspend fun getAllStatus(): List<Status> {
        return try {
            val querySnapshot = firestore
                .collection(DatabaseContants.STATUS_COLLECTION)
                .get()
                .await()
            querySnapshot.documents.mapNotNull { document ->
                document.data?.let { Status.fromMap(it) }
            }
        } catch (e: Exception) {
            Log.d(TAG, "getAllStatus: ${e.message}", e)
            emptyList()
        }
    }
}