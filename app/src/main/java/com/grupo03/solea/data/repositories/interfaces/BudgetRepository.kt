package com.grupo03.solea.data.repositories.interfaces

import com.grupo03.solea.data.models.Budget
import com.grupo03.solea.data.models.Status
import com.grupo03.solea.utils.RepositoryResult

/**
 * Repository interface for managing budget limits.
 *
 * Budgets allow users to set spending limits on specific categories
 * for defined time periods.
 */
interface BudgetRepository {

    /**
     * Creates a new budget.
     *
     * @param budget The budget to create
     * @return Result containing the created budget or an error
     */
    suspend fun createBudget(budget: Budget): RepositoryResult<Budget>

    /**
     * Retrieves all budgets for a specific user.
     *
     * @param userId User's unique identifier
     * @return Result containing list of user's budgets or an error
     */
    suspend fun getAllBudgetsByUser(userId: String): RepositoryResult<List<Budget>>

    /**
     * Retrieves a budget by its ID.
     *
     * @param budgetId Unique identifier of the budget
     * @return Result containing the budget if found (nullable) or an error
     */
    suspend fun getBudgetById(budgetId: String): RepositoryResult<Budget?>

    /**
     * Updates an existing budget.
     *
     * @param budget The budget with updated data
     * @return Result containing the updated budget or an error
     */
    suspend fun updateBudget(budget: Budget): RepositoryResult<Budget>

    /**
     * Deletes a budget by its ID.
     *
     * @param budgetId Unique identifier of the budget to delete
     * @return Result indicating success or error
     */
    suspend fun deleteBudget(budgetId: String): RepositoryResult<Unit>

    /**
     * Finds a budget for a specific user and category.
     *
     * @param userId User's unique identifier
     * @param categoryName Name of the category
     * @return Result containing the budget if found (nullable) or an error
     */
    suspend fun getBudgetByCategory(userId: String, categoryName: String): RepositoryResult<Budget?>

    /**
     * Retrieves all available budget statuses.
     *
     * Statuses are predefined in the system (e.g., "active", "expired", "exceeded").
     *
     * @return Result containing list of all statuses or an error
     */
    suspend fun getAllStatus(): RepositoryResult<List<Status>>
}
