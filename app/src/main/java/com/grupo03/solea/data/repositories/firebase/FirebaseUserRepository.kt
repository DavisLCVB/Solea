package com.grupo03.solea.data.repositories.firebase

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.grupo03.solea.data.models.User
import com.grupo03.solea.data.repositories.interfaces.UserRepository
import com.grupo03.solea.utils.RepositoryResult
import com.grupo03.solea.utils.UserError
import kotlinx.coroutines.tasks.await

/**
 * Firebase Firestore implementation of UserRepository.
 *
 * Manages user profile data in Firestore at /users/{uid}.
 * User profiles are created after Firebase Authentication and contain
 * additional user data including currency, which is immutable after creation.
 *
 * @property firestore Firebase Firestore instance
 */
class FirebaseUserRepository(
    private val firestore: FirebaseFirestore
) : UserRepository {

    private companion object {
        const val TAG = "FirebaseUserRepository"
        const val USERS_COLLECTION = "users"
        const val FIELD_UID = "uid"
        const val FIELD_EMAIL = "email"
        const val FIELD_DISPLAY_NAME = "displayName"
        const val FIELD_PHOTO_URL = "photoUrl"
        const val FIELD_CURRENCY = "currency"
        const val FIELD_CREATED_AT = "createdAt"
    }

    private val usersCollection = firestore.collection(USERS_COLLECTION)

    override suspend fun createUserProfile(user: User): RepositoryResult<User> {
        return try {
            val userMap = mapOf(
                FIELD_UID to user.uid,
                FIELD_EMAIL to user.email,
                FIELD_DISPLAY_NAME to user.displayName,
                FIELD_PHOTO_URL to user.photoUrl,
                FIELD_CURRENCY to user.currency,
                FIELD_CREATED_AT to FieldValue.serverTimestamp()
            )

            usersCollection.document(user.uid).set(userMap).await()
            Log.d(TAG, "User profile created successfully for uid: ${user.uid}")
            RepositoryResult.Success(user)
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Firestore error creating user profile: ${e.code}", e)
            RepositoryResult.Error(UserError.CREATION_FAILED)
        } catch (e: Exception) {
            Log.e(TAG, "Unknown error creating user profile", e)
            RepositoryResult.Error(UserError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getUserProfile(uid: String): RepositoryResult<User> {
        return try {
            val document = usersCollection.document(uid).get().await()

            if (!document.exists()) {
                Log.w(TAG, "User profile not found for uid: $uid")
                return RepositoryResult.Error(UserError.NOT_FOUND)
            }

            val user = User(
                uid = document.getString(FIELD_UID) ?: "",
                email = document.getString(FIELD_EMAIL) ?: "",
                displayName = document.getString(FIELD_DISPLAY_NAME),
                photoUrl = document.getString(FIELD_PHOTO_URL),
                currency = document.getString(FIELD_CURRENCY) ?: "PEN"
            )

            Log.d(TAG, "User profile fetched successfully for uid: $uid")
            RepositoryResult.Success(user)
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Firestore error fetching user profile: ${e.code}", e)
            RepositoryResult.Error(UserError.FETCH_FAILED)
        } catch (e: Exception) {
            Log.e(TAG, "Unknown error fetching user profile", e)
            RepositoryResult.Error(UserError.UNKNOWN_ERROR)
        }
    }

    override suspend fun updateUserProfile(
        uid: String,
        displayName: String?,
        photoUrl: String?
    ): RepositoryResult<User> {
        return try {
            // Only update mutable fields (NOT currency)
            val updates = mutableMapOf<String, Any?>()
            displayName?.let { updates[FIELD_DISPLAY_NAME] = it }
            photoUrl?.let { updates[FIELD_PHOTO_URL] = it }

            if (updates.isEmpty()) {
                Log.d(TAG, "No updates provided for uid: $uid")
                return getUserProfile(uid)
            }

            usersCollection.document(uid).update(updates).await()
            Log.d(TAG, "User profile updated successfully for uid: $uid")

            // Fetch and return the updated profile
            getUserProfile(uid)
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Firestore error updating user profile: ${e.code}", e)
            RepositoryResult.Error(UserError.UPDATE_FAILED)
        } catch (e: Exception) {
            Log.e(TAG, "Unknown error updating user profile", e)
            RepositoryResult.Error(UserError.UNKNOWN_ERROR)
        }
    }
}
