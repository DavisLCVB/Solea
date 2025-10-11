package com.grupo03.solea.data.repositories.interfaces

import com.grupo03.solea.data.models.Budget
import com.grupo03.solea.data.models.Status
import com.grupo03.solea.utils.RepositoryResult

interface BudgetRepository {
    suspend fun createBudget(budget: Budget): RepositoryResult<Budget>
    suspend fun getAllBudgetsByUser(userId: String): RepositoryResult<List<Budget>>
    suspend fun getBudgetById(budgetId: String): RepositoryResult<Budget?>
    suspend fun updateBudget(budget: Budget): RepositoryResult<Budget>
    suspend fun deleteBudget(budgetId: String): RepositoryResult<Unit>
    suspend fun getBudgetByCategory(userId: String, categoryName: String): RepositoryResult<Budget?>
    suspend fun getAllStatus(): RepositoryResult<List<Status>>
}
