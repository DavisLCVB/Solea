package com.grupo03.solea.data.repositories.interfaces

import com.grupo03.solea.data.models.Expense
import com.grupo03.solea.data.models.ExpenseDetails
import com.grupo03.solea.data.models.Income
import com.grupo03.solea.data.models.IncomeDetails
import com.grupo03.solea.data.models.Item
import com.grupo03.solea.data.models.Movement
import com.grupo03.solea.data.models.MovementType
import com.grupo03.solea.data.models.Receipt
import com.grupo03.solea.data.models.Save
import com.grupo03.solea.data.models.SaveDetails
import com.grupo03.solea.data.models.Source
import com.grupo03.solea.utils.RepositoryResult
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Repository interface for managing financial movements (expenses and incomes).
 *
 * This repository handles all operations related to financial transactions,
 * including CRUD operations, specialized expense/income creation, queries,
 * analytics, and real-time data streaming.
 */
interface MovementRepository {

    /**
     * Creates a new movement record.
     *
     * @param movement The movement to create
     * @return Result containing the created movement or an error
     */
    suspend fun createMovement(movement: Movement): RepositoryResult<Movement>

    /**
     * Retrieves a movement by its ID.
     *
     * @param id Unique identifier of the movement
     * @return Result containing the movement if found (nullable) or an error
     */
    suspend fun getMovementById(id: String): RepositoryResult<Movement?>

    /**
     * Updates an existing movement.
     *
     * @param movement The movement with updated data
     * @return Result containing the updated movement or an error
     */
    suspend fun updateMovement(movement: Movement): RepositoryResult<Movement>

    /**
     * Deletes a movement by its ID.
     *
     * @param id Unique identifier of the movement to delete
     * @return Result indicating success or error
     */
    suspend fun deleteMovement(id: String): RepositoryResult<Unit>

    // ==================== Queries by User ====================

    /**
     * Retrieves all movements for a specific user.
     *
     * @param userUid User's unique identifier
     * @return Result containing list of movements or an error
     */
    suspend fun getMovementsByUserId(userUid: String): RepositoryResult<List<Movement>>

    /**
     * Retrieves movements for a user filtered by type.
     *
     * @param userUid User's unique identifier
     * @param type Type of movement (EXPENSE or INCOME)
     * @return Result containing filtered list of movements or an error
     */
    suspend fun getMovementsByUserAndType(
        userUid: String,
        type: MovementType
    ): RepositoryResult<List<Movement>>

    /**
     * Retrieves movements for a user within a date range.
     *
     * @param userUid User's unique identifier
     * @param startDate Start of the date range (inclusive)
     * @param endDate End of the date range (inclusive)
     * @return Result containing list of movements in the date range or an error
     */
    suspend fun getMovementsByUserAndDateRange(
        userUid: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): RepositoryResult<List<Movement>>

    /**
     * Retrieves movements for a user filtered by category.
     *
     * @param userUid User's unique identifier
     * @param category Category name to filter by
     * @return Result containing filtered list of movements or an error
     */
    suspend fun getMovementsByUserAndCategory(
        userUid: String,
        category: String
    ): RepositoryResult<List<Movement>>

    // ==================== Expense-specific Operations ====================

    /**
     * Creates a complete expense with all related entities.
     *
     * This creates the movement, expense, source, and either an item or receipt with items
     * in a coordinated manner.
     *
     * @param movement The base movement record (type must be EXPENSE)
     * @param source The source record indicating where the expense came from
     * @param item The item record (required if source type is ITEM)
     * @param receipt The receipt record (required if source type is RECEIPT)
     * @param receiptItems List of items in the receipt (optional, only for receipt sources)
     * @return Result containing complete expense details or an error
     */
    suspend fun createExpense(
        movement: Movement,
        source: Source,
        item: Item?,
        receipt: Receipt?,
        receiptItems: List<Item>?
    ): RepositoryResult<ExpenseDetails>

    suspend fun getExpenseById(id: String): RepositoryResult<ExpenseDetails?>
    suspend fun getExpensesByUserId(userUid: String): RepositoryResult<List<ExpenseDetails>>

    suspend fun createIncome(movement: Movement): RepositoryResult<IncomeDetails>

    suspend fun createIncome(income: Income): RepositoryResult<Income>

    suspend fun getIncomeById(id: String): RepositoryResult<IncomeDetails?>

    suspend fun getIncomesByUserId(userUid: String): RepositoryResult<List<IncomeDetails>>

    suspend fun createSaving(
        movement: Movement,
        save: Save
    ): RepositoryResult<SaveDetails>

    suspend fun getSavingById(id: String): RepositoryResult<SaveDetails?>

    suspend fun getSavingsByUserId(userUid: String): RepositoryResult<List<SaveDetails>>

    suspend fun getSavingsByGoalId(goalId: String): RepositoryResult<List<SaveDetails>>

    suspend fun deleteSavingsByGoalId(goalId: String): RepositoryResult<Unit>

    suspend fun createExpense(expense: Expense): RepositoryResult<Expense>

    suspend fun createSource(source: Source): RepositoryResult<Source>

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

    fun observeIncomesByUserId(userUid: String): Flow<RepositoryResult<List<IncomeDetails>>>

    fun observeExpensesByUserId(userUid: String): Flow<RepositoryResult<List<ExpenseDetails>>>

    fun observeSavingsByUserId(userUid: String): Flow<RepositoryResult<List<SaveDetails>>>

}
