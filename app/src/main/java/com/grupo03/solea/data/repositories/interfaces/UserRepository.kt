package com.grupo03.solea.data.repositories.interfaces

import com.grupo03.solea.data.models.User
import com.grupo03.solea.utils.RepositoryResult

/**
 * Repository interface for user profile operations in Firestore.
 *
 * This repository manages user profile data stored in Firestore,
 * separate from Firebase Authentication. It handles CRUD operations
 * for user profiles including currency and other user-specific settings.
 *
 * User profiles are created after successful Firebase Authentication
 * and contain additional user data not available in Firebase Auth.
 */
interface UserRepository {
    /**
     * Creates a new user profile document in Firestore.
     *
     * Called during user registration after Firebase Auth account creation.
     * Creates a document at /users/{uid} with user profile data including
     * currency, which is set once during registration and cannot be changed.
     *
     * @param user The user profile to create (must include uid, email, and currency)
     * @return RepositoryResult.Success with the created user, or RepositoryResult.Error
     */
    suspend fun createUserProfile(user: User): RepositoryResult<User>

    /**
     * Retrieves a user profile from Firestore.
     *
     * Called after sign-in to load the complete user profile including
     * currency and other settings not available in Firebase Auth.
     *
     * @param uid The unique identifier of the user
     * @return RepositoryResult.Success with the user profile, or RepositoryResult.Error if not found
     */
    suspend fun getUserProfile(uid: String): RepositoryResult<User>

    /**
     * Updates mutable fields of a user profile.
     *
     * Only displayName and photoUrl can be updated. Currency is immutable
     * after profile creation and cannot be changed through this method.
     *
     * @param uid The unique identifier of the user
     * @param displayName Optional new display name
     * @param photoUrl Optional new photo URL
     * @return RepositoryResult.Success with the updated user, or RepositoryResult.Error
     */
    suspend fun updateUserProfile(
        uid: String,
        displayName: String? = null,
        photoUrl: String? = null
    ): RepositoryResult<User>
}
