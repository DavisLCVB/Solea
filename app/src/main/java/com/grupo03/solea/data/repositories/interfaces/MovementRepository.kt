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

    // ==================== Basic CRUD ====================

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

    /**
     * Retrieves complete expense details by expense ID.
     *
     * @param id Unique identifier of the expense
     * @return Result containing complete expense details if found (nullable) or an error
     */
    suspend fun getExpenseById(id: String): RepositoryResult<ExpenseDetails?>

    /**
     * Retrieves all expenses for a specific user with complete details.
     *
     * @param userUid User's unique identifier
     * @return Result containing list of expense details or an error
     */
    suspend fun getExpensesByUserId(userUid: String): RepositoryResult<List<ExpenseDetails>>

    // ==================== Income-specific Operations ====================

    /**
     * Creates a complete income with movement.
     *
     * @param movement The base movement record (type must be INCOME)
     * @return Result containing complete income details or an error
     */
    suspend fun createIncome(movement: Movement): RepositoryResult<IncomeDetails>

    /**
     * Creates just an income record (helper method).
     *
     * @param income The income record to create
     * @return Result containing the created income or an error
     */
    suspend fun createIncome(income: Income): RepositoryResult<Income>

    /**
     * Retrieves complete income details by income ID.
     *
     * @param id Unique identifier of the income
     * @return Result containing complete income details if found (nullable) or an error
     */
    suspend fun getIncomeById(id: String): RepositoryResult<IncomeDetails?>

    /**
     * Retrieves all incomes for a specific user with complete details.
     *
     * @param userUid User's unique identifier
     * @return Result containing list of income details or an error
     */
    suspend fun getIncomesByUserId(userUid: String): RepositoryResult<List<IncomeDetails>>

    // ==================== Saving-specific Operations ====================

    /**
     * Creates a complete saving with movement.
     *
     * @param movement The base movement record (type must be SAVING)
     * @param save The save record containing goal and amount info
     * @return Result containing complete saving details or an error
     */
    suspend fun createSaving(
        movement: Movement,
        save: Save
    ): RepositoryResult<SaveDetails>

    /**
     * Retrieves complete saving details by saving ID.
     *
     * @param id Unique identifier of the saving
     * @return Result containing complete saving details if found (nullable) or an error
     */
    suspend fun getSavingById(id: String): RepositoryResult<SaveDetails?>

    /**
     * Retrieves all savings for a specific user with complete details.
     *
     * @param userUid User's unique identifier
     * @return Result containing list of saving details or an error
     */
    suspend fun getSavingsByUserId(userUid: String): RepositoryResult<List<SaveDetails>>

    /**
     * Retrieves all savings for a specific goal with complete details.
     *
     * @param goalId Goal's unique identifier
     * @return Result containing list of saving details or an error
     */
    suspend fun getSavingsByGoalId(goalId: String): RepositoryResult<List<SaveDetails>>

    /**
     * Deletes all savings associated with a goal, including their movements.
     * This will restore the money to the user's balance.
     *
     * @param goalId Goal's unique identifier
     * @return Result indicating success or error
     */
    suspend fun deleteSavingsByGoalId(goalId: String): RepositoryResult<Unit>

    // ==================== Helper Operations for Expenses ====================

    /**
     * Creates just an expense record (helper method).
     *
     * @param expense The expense record to create
     * @return Result containing the created expense or an error
     */
    suspend fun createExpense(expense: Expense): RepositoryResult<Expense>

    /**
     * Creates a source record (helper method).
     *
     * @param source The source record to create
     * @return Result containing the created source or an error
     */
    suspend fun createSource(source: Source): RepositoryResult<Source>

    // ==================== Analytics ====================

    /**
     * Calculates total expenses for a user.
     *
     * @param userUid User's unique identifier
     * @return Result containing the total expense amount or an error
     */
    suspend fun getTotalExpensesByUser(userUid: String): RepositoryResult<Double>

    /**
     * Calculates total incomes for a user.
     *
     * @param userUid User's unique identifier
     * @return Result containing the total income amount or an error
     */
    suspend fun getTotalIncomesByUser(userUid: String): RepositoryResult<Double>

    /**
     * Calculates balance (incomes - expenses) for a user.
     *
     * @param userUid User's unique identifier
     * @return Result containing the balance or an error
     */
    suspend fun getBalanceByUser(userUid: String): RepositoryResult<Double>

    /**
     * Groups and sums expenses by category for a user.
     *
     * @param userUid User's unique identifier
     * @return Result containing a map of category names to total amounts or an error
     */
    suspend fun getExpensesByCategory(userUid: String): RepositoryResult<Map<String, Double>>

    /**
     * Calculates total expenses for a specific month.
     *
     * @param userUid User's unique identifier
     * @param year Year to query
     * @param month Month to query (1-12)
     * @return Result containing the monthly expense total or an error
     */
    suspend fun getMonthlyExpensesByUser(
        userUid: String,
        year: Int,
        month: Int
    ): RepositoryResult<Double>

    /**
     * Calculates total incomes for a specific month.
     *
     * @param userUid User's unique identifier
     * @param year Year to query
     * @param month Month to query (1-12)
     * @return Result containing the monthly income total or an error
     */
    suspend fun getMonthlyIncomesByUser(
        userUid: String,
        year: Int,
        month: Int
    ): RepositoryResult<Double>

    // ==================== Real-time Observers ====================

    /**
     * Observes incomes for a user in real-time.
     *
     * Returns a Flow that emits updates whenever the user's incomes change.
     *
     * @param userUid User's unique identifier
     * @return Flow of results containing lists of income details
     */
    fun observeIncomesByUserId(userUid: String): Flow<RepositoryResult<List<IncomeDetails>>>

    /**
     * Observes expenses for a user in real-time.
     *
     * Returns a Flow that emits updates whenever the user's expenses change.
     *
     * @param userUid User's unique identifier
     * @return Flow of results containing lists of expense details
     */
    fun observeExpensesByUserId(userUid: String): Flow<RepositoryResult<List<ExpenseDetails>>>

    /**
     * Observes savings for a user in real-time.
     *
     * Returns a Flow that emits updates whenever the user's savings change.
     *
     * @param userUid User's unique identifier
     * @return Flow of results containing lists of saving details
     */
    fun observeSavingsByUserId(userUid: String): Flow<RepositoryResult<List<SaveDetails>>>

}
