package com.grupo03.solea.data.repositories

import com.grupo03.solea.data.models.Budget
import com.grupo03.solea.data.models.Status

interface BudgetsRepository {
    suspend fun createBudget(budget: Budget): Budget?
    suspend fun getAllBudgetsByUser(userId: String): List<Budget>
    suspend fun updateBudget(budget: Budget): Boolean
    suspend fun deleteBudget(budgetId: String): Boolean
    suspend fun getBudgetByMovementType(userId: String, movementTypeId: String): Budget?
    suspend fun getAllStatus(): List<Status>
}