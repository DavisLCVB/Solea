package com.grupo03.solea.data.repositories.interfaces

import com.grupo03.solea.data.models.Category
import com.grupo03.solea.utils.RepositoryResult
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing expense categories.
 *
 * Categories can be either global (default, available to all users) or
 * user-specific (custom categories created by individual users).
 */
interface CategoryRepository {

    /**
     * Creates a new category.
     *
     * @param category The category to create
     * @return Result containing the created category or an error
     */
    suspend fun createCategory(category: Category): RepositoryResult<Category>

    /**
     * Retrieves all categories (both default and user-specific).
     *
     * @return Result containing list of all categories or an error
     */
    suspend fun getAllCategories(): RepositoryResult<List<Category>>

    /**
     * Retrieves categories specific to a user.
     *
     * This returns only the custom categories created by the specified user.
     *
     * @param userId User's unique identifier
     * @return Result containing list of user's categories or an error
     */
    suspend fun getCategoriesByUser(userId: String): RepositoryResult<List<Category>>

    /**
     * Retrieves default/global categories.
     *
     * Default categories are predefined and available to all users.
     *
     * @return Result containing list of default categories or an error
     */
    suspend fun getDefaultCategories(): RepositoryResult<List<Category>>

    /**
     * Finds a category by its name.
     *
     * @param name Name of the category to search for
     * @return Result containing the category if found (nullable) or an error
     */
    suspend fun getCategoryByName(name: String): RepositoryResult<Category?>

    /**
     * Retrieves a category by its ID.
     *
     * @param categoryId Unique identifier of the category
     * @return Result containing the category if found (nullable) or an error
     */
    suspend fun getCategoryById(categoryId: String): RepositoryResult<Category?>

    /**
     * Updates an existing category.
     *
     * @param category The category with updated data
     * @return Result containing the updated category or an error
     */
    suspend fun updateCategory(category: Category): RepositoryResult<Category>

    /**
     * Deletes a category by its ID.
     *
     * @param categoryId Unique identifier of the category to delete
     * @return Result indicating success or error
     */
    suspend fun deleteCategory(categoryId: String): RepositoryResult<Unit>

    /**
     * Observes categories for a user in real-time.
     *
     * Returns a Flow that emits updates whenever the user's categories change.
     *
     * @param userId User's unique identifier
     * @return Flow of results containing lists of categories
     */
    fun observeCategoriesByUser(userId: String): Flow<RepositoryResult<List<Category>>>
}
