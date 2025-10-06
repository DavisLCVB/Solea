package com.grupo03.solea.data.repositories.interfaces

import com.grupo03.solea.data.models.Expense
import com.grupo03.solea.data.models.ExpenseDetails
import com.grupo03.solea.data.models.Income
import com.grupo03.solea.data.models.IncomeDetails
import com.grupo03.solea.data.models.Item
import com.grupo03.solea.data.models.Movement
import com.grupo03.solea.data.models.MovementType
import com.grupo03.solea.data.models.Receipt
import com.grupo03.solea.data.models.Source
import com.grupo03.solea.utils.RepositoryResult
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface MovementRepository {

    // Basic CRUD
    suspend fun createMovement(movement: Movement): RepositoryResult<Movement>
    suspend fun getMovementById(id: String): RepositoryResult<Movement?>
    suspend fun updateMovement(movement: Movement): RepositoryResult<Movement>
    suspend fun deleteMovement(id: String): RepositoryResult<Unit>

    // Queries by user
    suspend fun getMovementsByUserId(userUid: String): RepositoryResult<List<Movement>>
    suspend fun getMovementsByUserAndType(
        userUid: String,
        type: MovementType
    ): RepositoryResult<List<Movement>>

    suspend fun getMovementsByUserAndDateRange(
        userUid: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): RepositoryResult<List<Movement>>

    suspend fun getMovementsByUserAndCategory(
        userUid: String,
        category: String
    ): RepositoryResult<List<Movement>>

    // Expense-specific operations
    suspend fun createExpense(
        movement: Movement,
        source: Source,
        item: Item?,
        receipt: Receipt?,
        receiptItems: List<Item>?
    ): RepositoryResult<ExpenseDetails>

    suspend fun getExpenseById(id: String): RepositoryResult<ExpenseDetails?>
    suspend fun getExpensesByUserId(userUid: String): RepositoryResult<List<ExpenseDetails>>

    // Income-specific operations
    suspend fun createIncome(movement: Movement): RepositoryResult<IncomeDetails>
    suspend fun createIncome(income: Income): RepositoryResult<Income>
    suspend fun getIncomeById(id: String): RepositoryResult<IncomeDetails?>
    suspend fun getIncomesByUserId(userUid: String): RepositoryResult<List<IncomeDetails>>

    // Helper operations for expenses
    suspend fun createExpense(expense: Expense): RepositoryResult<Expense>
    suspend fun createSource(source: Source): RepositoryResult<Source>

    // Analytics
    suspend fun getTotalExpensesByUser(userUid: String): RepositoryResult<Double>
    suspend fun getTotalIncomesByUser(userUid: String): RepositoryResult<Double>
    suspend fun getBalanceByUser(userUid: String): RepositoryResult<Double>
    suspend fun getExpensesByCategory(userUid: String): RepositoryResult<Map<String, Double>>
    suspend fun getMonthlyExpensesByUser(
        userUid: String,
        year: Int,
        month: Int
    ): RepositoryResult<Double>

    suspend fun getMonthlyIncomesByUser(
        userUid: String,
        year: Int,
        month: Int
    ): RepositoryResult<Double>

    // Real-time observers
    fun observeIncomesByUserId(userUid: String): Flow<RepositoryResult<List<IncomeDetails>>>
    fun observeExpensesByUserId(userUid: String): Flow<RepositoryResult<List<ExpenseDetails>>>
}
