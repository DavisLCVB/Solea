package com.grupo03.solea.data.repositories.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.grupo03.solea.data.models.Budget
import com.grupo03.solea.data.models.Status
import com.grupo03.solea.data.repositories.interfaces.BudgetRepository
import com.grupo03.solea.utils.DatabaseContants
import com.grupo03.solea.utils.BudgetError
import com.grupo03.solea.utils.RepositoryResult
import com.grupo03.solea.utils.mapFirestoreException
import kotlinx.coroutines.tasks.await

class FirebaseBudgetRepository(
    private val firestore: FirebaseFirestore
) : BudgetRepository {
    companion object {
        private const val TAG = "FirebaseBudgetRepo"
    }

    override suspend fun createBudget(budget: Budget): RepositoryResult<Budget> {
        return try {
            Log.d(TAG, "createBudget: Starting budget creation for userId=${budget.userId}, category=${budget.category}")
            val doc = firestore
                .collection(DatabaseContants.BUDGETS_COLLECTION)
                .document()
            val budgetWithId = budget.copy(id = doc.id)
            val budgetMap = budgetWithId.toMap()
            Log.d(TAG, "createBudget: Budget map to save: $budgetMap")
            Log.d(TAG, "createBudget: Document ID: ${doc.id}")
            doc.set(budgetMap).await()
            Log.d(TAG, "createBudget: Budget created successfully with ID: ${doc.id}")
            RepositoryResult.Success(budgetWithId)
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "createBudget: FirebaseFirestoreException - code=${e.code}, message=${e.message}", e)
            RepositoryResult.Error(mapFirestoreException(e, BudgetError.CREATION_FAILED))
        } catch (e: Exception) {
            Log.e(TAG, "createBudget: Exception - ${e.javaClass.simpleName}: ${e.message}", e)
            RepositoryResult.Error(BudgetError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getAllBudgetsByUser(userId: String): RepositoryResult<List<Budget>> {
        return try {
            val querySnapshot = firestore
                .collection(DatabaseContants.BUDGETS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val budgets = querySnapshot.documents.mapNotNull { document ->
                Budget.fromMap(document.data ?: emptyMap())
            }
            RepositoryResult.Success(budgets)
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "getAllBudgetsByUser: FirebaseFirestoreException", e)
            RepositoryResult.Error(mapFirestoreException(e, BudgetError.FETCH_FAILED))
        } catch (e: Exception) {
            Log.e(TAG, "getAllBudgetsByUser: Exception", e)
            RepositoryResult.Error(BudgetError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getBudgetById(budgetId: String): RepositoryResult<Budget?> {
        return try {
            val docSnapshot = firestore
                .collection(DatabaseContants.BUDGETS_COLLECTION)
                .document(budgetId)
                .get()
                .await()
            val budget = Budget.fromMap(docSnapshot.data ?: emptyMap())
            RepositoryResult.Success(budget)
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "getBudgetById: FirebaseFirestoreException", e)
            RepositoryResult.Error(mapFirestoreException(e, BudgetError.FETCH_FAILED))
        } catch (e: Exception) {
            Log.e(TAG, "getBudgetById: Exception", e)
            RepositoryResult.Error(BudgetError.UNKNOWN_ERROR)
        }
    }

    override suspend fun updateBudget(budget: Budget): RepositoryResult<Budget> {
        return try {
            val docRef = firestore
                .collection(DatabaseContants.BUDGETS_COLLECTION)
                .document(budget.id)
            docRef.set(budget.toMap()).await()
            RepositoryResult.Success(budget)
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "updateBudget: FirebaseFirestoreException", e)
            RepositoryResult.Error(mapFirestoreException(e, BudgetError.UPDATE_FAILED))
        } catch (e: Exception) {
            Log.e(TAG, "updateBudget: Exception", e)
            RepositoryResult.Error(BudgetError.UNKNOWN_ERROR)
        }
    }

    override suspend fun deleteBudget(budgetId: String): RepositoryResult<Unit> {
        return try {
            val docRef = firestore
                .collection(DatabaseContants.BUDGETS_COLLECTION)
                .document(budgetId)
            docRef.delete().await()
            RepositoryResult.Success(Unit)
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "deleteBudget: FirebaseFirestoreException", e)
            RepositoryResult.Error(mapFirestoreException(e, BudgetError.DELETE_FAILED))
        } catch (e: Exception) {
            Log.e(TAG, "deleteBudget: Exception", e)
            RepositoryResult.Error(BudgetError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getBudgetByCategory(userId: String, categoryName: String): RepositoryResult<Budget?> {
        return try {
            val querySnapshot = firestore
                .collection(DatabaseContants.BUDGETS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("category", categoryName)
                .get()
                .await()
            val budget = querySnapshot.documents.firstOrNull()?.let {
                Budget.fromMap(it.data ?: emptyMap())
            }
            RepositoryResult.Success(budget)
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "getBudgetByCategory: FirebaseFirestoreException", e)
            RepositoryResult.Error(mapFirestoreException(e, BudgetError.FETCH_FAILED))
        } catch (e: Exception) {
            Log.e(TAG, "getBudgetByCategory: Exception", e)
            RepositoryResult.Error(BudgetError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getAllStatus(): RepositoryResult<List<Status>> {
        return try {
            val querySnapshot = firestore
                .collection(DatabaseContants.STATUS_COLLECTION)
                .get()
                .await()
            val statuses = querySnapshot.documents.mapNotNull { document ->
                Status.fromMap(document.data ?: emptyMap())
            }
            RepositoryResult.Success(statuses)
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "getAllStatus: FirebaseFirestoreException", e)
            RepositoryResult.Error(mapFirestoreException(e, BudgetError.FETCH_FAILED))
        } catch (e: Exception) {
            Log.e(TAG, "getAllStatus: Exception", e)
            RepositoryResult.Error(BudgetError.UNKNOWN_ERROR)
        }
    }

}
