package com.grupo03.solea.data.repositories

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.grupo03.solea.data.models.User
import com.grupo03.solea.utils.DatabaseContants
import kotlinx.coroutines.tasks.await

class FirebaseUsersRepository(
    private val firestore: FirebaseFirestore
) : UsersRepository {
    override suspend fun createUser(user: User): User? {
        return try {
            val documentRef = firestore.collection(DatabaseContants.USERS_COLLECTION).document()
            val userWithId = user.copy(uid = documentRef.id)
            documentRef.set(userWithId.let {
                mapOf(
                    "uid" to it.uid,
                    "displayName" to it.displayName,
                    "email" to it.email
                )
            })
            userWithId
        } catch (e: Exception) {
            Log.d("FirebaseUsersRepository", "Error creating user: ${e.message}")
            null
        }
    }

    override suspend fun getDisplayName(uid: String): String? {
        return try {
            val querySnapshot = firestore.collection(DatabaseContants.USERS_COLLECTION)
                .whereEqualTo("uid", uid)
                .get().await()
            if (!querySnapshot.isEmpty) {
                querySnapshot.documents[0].getString("displayName")
            } else {
                null
            }
        } catch (e: Exception) {
            Log.d("FirebaseUsersRepository", "Error fetching display name: ${e.message}")
            null
        }
    }
}