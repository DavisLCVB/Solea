package com.grupo03.solea.data.repositories

import com.grupo03.solea.data.models.User

interface UsersRepository {
    suspend fun createUser(user: User): User?
    suspend fun getDisplayName(uid: String): String?
}