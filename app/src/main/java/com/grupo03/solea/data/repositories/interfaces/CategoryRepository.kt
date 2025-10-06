package com.grupo03.solea.data.repositories.interfaces

import com.grupo03.solea.data.models.Category
import com.grupo03.solea.utils.RepositoryResult
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    suspend fun createCategory(category: Category): RepositoryResult<Category>
    suspend fun getAllCategories(): RepositoryResult<List<Category>>
    suspend fun getCategoriesByUser(userId: String): RepositoryResult<List<Category>>
    suspend fun getDefaultCategories(): RepositoryResult<List<Category>>
    suspend fun getCategoryByName(name: String): RepositoryResult<Category?>
    suspend fun getCategoryById(categoryId: String): RepositoryResult<Category?>
    suspend fun updateCategory(category: Category): RepositoryResult<Category>
    suspend fun deleteCategory(categoryId: String): RepositoryResult<Unit>

    // Real-time observers
    fun observeCategoriesByUser(userId: String): Flow<RepositoryResult<List<Category>>>
}
