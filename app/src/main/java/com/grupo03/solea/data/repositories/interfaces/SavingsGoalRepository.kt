package com.grupo03.solea.data.repositories.interfaces

import com.grupo03.solea.data.models.SavingsGoal
import com.grupo03.solea.utils.RepositoryResult
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing savings goals.
 */
interface SavingsGoalRepository {

    /**
     * Creates a new savings goal.
     */
    suspend fun createGoal(goal: SavingsGoal): RepositoryResult<SavingsGoal>

    /**
     * Retrieves a goal by its ID.
     */
    suspend fun getGoalById(goalId: String): RepositoryResult<SavingsGoal?>

    /**
     * Retrieves all goals for a specific user.
     */
    suspend fun getGoalsByUser(userId: String): RepositoryResult<List<SavingsGoal>>

    /**
     * Updates an existing goal.
     */
    suspend fun updateGoal(goal: SavingsGoal): RepositoryResult<SavingsGoal>

    /**
     * Deletes a goal by its ID.
     */
    suspend fun deleteGoal(goalId: String): RepositoryResult<Unit>

    /**
     * Updates the current saved amount for a goal.
     */
    suspend fun updateCurrentAmount(goalId: String, amount: Double): RepositoryResult<SavingsGoal>

    /**
     * Observes goals for a user in real-time.
     */
    fun observeGoalsByUser(userId: String): Flow<RepositoryResult<List<SavingsGoal>>>
}