package com.grupo03.solea.data.repositories.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.grupo03.solea.data.models.Category
import com.grupo03.solea.data.repositories.interfaces.CategoryRepository
import com.grupo03.solea.utils.DatabaseContants
import com.grupo03.solea.utils.CategoryError
import com.grupo03.solea.utils.RepositoryResult
import com.grupo03.solea.utils.mapFirestoreException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseCategoryRepository(
    private val firestore: FirebaseFirestore
) : CategoryRepository {
    companion object {
        private const val TAG = "FirebaseCategoryRepo"
    }

    override suspend fun createCategory(category: Category): RepositoryResult<Category> {
        return try {
            val doc = firestore
                .collection(DatabaseContants.CATEGORIES_COLLECTION)
                .document()
            val categoryWithId = category.copy(id = doc.id)
            doc.set(categoryWithId).await()
            RepositoryResult.Success(categoryWithId)
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "createCategory: FirebaseFirestoreException", e)
            RepositoryResult.Error(mapFirestoreException(e, CategoryError.CREATION_FAILED))
        } catch (e: Exception) {
            Log.e(TAG, "createCategory: Exception", e)
            RepositoryResult.Error(CategoryError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getAllCategories(): RepositoryResult<List<Category>> {
        return try {
            val querySnapshot = firestore
                .collection(DatabaseContants.CATEGORIES_COLLECTION)
                .get()
                .await()
            val categories = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Category::class.java)
            }
            RepositoryResult.Success(categories)
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "getAllCategories: FirebaseFirestoreException", e)
            RepositoryResult.Error(mapFirestoreException(e, CategoryError.FETCH_FAILED))
        } catch (e: Exception) {
            Log.e(TAG, "getAllCategories: Exception", e)
            RepositoryResult.Error(CategoryError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getCategoriesByUser(userId: String): RepositoryResult<List<Category>> {
        return try {
            val querySnapshot = firestore
                .collection(DatabaseContants.CATEGORIES_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val categories = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Category::class.java)
            }
            RepositoryResult.Success(categories)
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "getCategoriesByUser: FirebaseFirestoreException", e)
            RepositoryResult.Error(mapFirestoreException(e, CategoryError.FETCH_FAILED))
        } catch (e: Exception) {
            Log.e(TAG, "getCategoriesByUser: Exception", e)
            RepositoryResult.Error(CategoryError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getDefaultCategories(): RepositoryResult<List<Category>> {
        return try {
            val querySnapshot = firestore
                .collection(DatabaseContants.CATEGORIES_COLLECTION)
                .whereEqualTo("userId", null)
                .get()
                .await()
            val categories = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Category::class.java)
            }
            RepositoryResult.Success(categories)
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "getDefaultCategories: FirebaseFirestoreException", e)
            RepositoryResult.Error(mapFirestoreException(e, CategoryError.FETCH_FAILED))
        } catch (e: Exception) {
            Log.e(TAG, "getDefaultCategories: Exception", e)
            RepositoryResult.Error(CategoryError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getCategoryById(categoryId: String): RepositoryResult<Category?> {
        return try {
            val docSnapshot = firestore
                .collection(DatabaseContants.CATEGORIES_COLLECTION)
                .document(categoryId)
                .get()
                .await()
            val category = docSnapshot.toObject(Category::class.java)
            RepositoryResult.Success(category)
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "getCategoryById: FirebaseFirestoreException", e)
            RepositoryResult.Error(mapFirestoreException(e, CategoryError.FETCH_FAILED))
        } catch (e: Exception) {
            Log.e(TAG, "getCategoryById: Exception", e)
            RepositoryResult.Error(CategoryError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getCategoryByName(name: String): RepositoryResult<Category?> {
        return try {
            val querySnapshot = firestore
                .collection(DatabaseContants.CATEGORIES_COLLECTION)
                .whereEqualTo("name", name)
                .get()
                .await()
            val category = querySnapshot.documents.firstOrNull()?.toObject(Category::class.java)
            RepositoryResult.Success(category)
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "getCategoryByName: FirebaseFirestoreException", e)
            RepositoryResult.Error(mapFirestoreException(e, CategoryError.FETCH_FAILED))
        } catch (e: Exception) {
            Log.e(TAG, "getCategoryByName: Exception", e)
            RepositoryResult.Error(CategoryError.UNKNOWN_ERROR)
        }
    }

    override suspend fun updateCategory(category: Category): RepositoryResult<Category> {
        return try {
            val docRef = firestore
                .collection(DatabaseContants.CATEGORIES_COLLECTION)
                .document(category.id)
            docRef.set(category).await()
            RepositoryResult.Success(category)
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "updateCategory: FirebaseFirestoreException", e)
            RepositoryResult.Error(mapFirestoreException(e, CategoryError.UPDATE_FAILED))
        } catch (e: Exception) {
            Log.e(TAG, "updateCategory: Exception", e)
            RepositoryResult.Error(CategoryError.UNKNOWN_ERROR)
        }
    }

    override suspend fun deleteCategory(categoryId: String): RepositoryResult<Unit> {
        return try {
            val docRef = firestore
                .collection(DatabaseContants.CATEGORIES_COLLECTION)
                .document(categoryId)
            docRef.delete().await()
            RepositoryResult.Success(Unit)
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "deleteCategory: FirebaseFirestoreException", e)
            RepositoryResult.Error(mapFirestoreException(e, CategoryError.DELETE_FAILED))
        } catch (e: Exception) {
            Log.e(TAG, "deleteCategory: Exception", e)
            RepositoryResult.Error(CategoryError.UNKNOWN_ERROR)
        }
    }

    // Real-time observers
    override fun observeCategoriesByUser(userId: String): Flow<RepositoryResult<List<Category>>> = callbackFlow {
        val listener = firestore
            .collection(DatabaseContants.CATEGORIES_COLLECTION)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "observeCategoriesByUser: FirebaseFirestoreException", error)
                    trySend(RepositoryResult.Error(mapFirestoreException(error, CategoryError.FETCH_FAILED)))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val categories = snapshot.documents.mapNotNull { document ->
                        document.toObject(Category::class.java)
                    }
                    trySend(RepositoryResult.Success(categories))
                }
            }

        awaitClose { listener.remove() }
    }

}
