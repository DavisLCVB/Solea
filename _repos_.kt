package com.example.expense.data.repository

import java.time.LocalDateTime

// ============================================================
// Data Classes
// ============================================================

data class Receipt(
    val id: String,
    val description: String,
    val datetime: LocalDateTime,
    val currency: String,
    val total: Double,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class Item(
    val id: String,
    val receiptId: String,
    val description: String,
    val quantity: Double,
    val currency: String,
    val unitPrice: Double,
    val totalPrice: Double,
    val category: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class Movement(
    val id: String,
    val userUid: String,
    val type: MovementType,
    val description: String,
    val datetime: LocalDateTime,
    val currency: String,
    val total: Double,
    val category: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class MovementType {
    EXPENSE,
    INCOME
}

data class ExpenseSource(
    val id: String,
    val sourceType: SourceType,
    val sourceItemId: String? = null,
    val sourceReceiptId: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class SourceType {
    ITEM,
    RECEIPT
}

data class Expense(
    val id: String,
    val movementId: String,
    val sourceId: String
)

data class Income(
    val id: String,
    val movementId: String
)

// Composite data classes for detailed views
data class ExpenseDetails(
    val expense: Expense,
    val movement: Movement,
    val source: ExpenseSource
)

data class IncomeDetails(
    val income: Income,
    val movement: Movement
)

// ============================================================
// Repository Interfaces
// ============================================================

/**
 * Repository interface for managing receipts
 */
interface ReceiptRepository {

    suspend fun createReceipt(receipt: Receipt): Result<Receipt>
    suspend fun getReceiptById(id: String): Result<Receipt?>
    suspend fun getReceiptsByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Result<List<Receipt>>

    suspend fun updateReceipt(receipt: Receipt): Result<Receipt>
    suspend fun deleteReceipt(id: String): Result<Unit>
}

/**
 * Repository interface for managing items
 */
interface ItemRepository {

    suspend fun createItem(item: Item): Result<Item>
    suspend fun getItemById(id: String): Result<Item?>
    suspend fun getItemsByReceiptId(receiptId: String): Result<List<Item>>
    suspend fun getItemsByCategory(category: String): Result<List<Item>>
    suspend fun updateItem(item: Item): Result<Item>
    suspend fun deleteItem(id: String): Result<Unit>
}

/**
 * Repository interface for managing movements (expenses and incomes)
 */
interface MovementRepository {

    // Basic CRUD
    suspend fun createMovement(movement: Movement): Result<Movement>
    suspend fun getMovementById(id: String): Result<Movement?>
    suspend fun updateMovement(movement: Movement): Result<Movement>
    suspend fun deleteMovement(id: String): Result<Unit>

    // Queries by user
    suspend fun getMovementsByUserId(userUid: String): Result<List<Movement>>
    suspend fun getMovementsByUserAndType(
        userUid: String,
        type: MovementType
    ): Result<List<Movement>>

    suspend fun getMovementsByUserAndDateRange(
        userUid: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Result<List<Movement>>

    suspend fun getMovementsByUserAndCategory(
        userUid: String,
        category: String
    ): Result<List<Movement>>

    // Expense-specific operations
    suspend fun createExpense(
        movement: Movement,
        source: ExpenseSource
    ): Result<ExpenseDetails>

    suspend fun getExpenseById(id: String): Result<ExpenseDetails?>
    suspend fun getExpensesByUserId(userUid: String): Result<List<ExpenseDetails>>

    // Income-specific operations
    suspend fun createIncome(movement: Movement): Result<IncomeDetails>
    suspend fun getIncomeById(id: String): Result<IncomeDetails?>
    suspend fun getIncomesByUserId(userUid: String): Result<List<IncomeDetails>>

    // Analytics
    suspend fun getTotalExpensesByUser(userUid: String): Result<Double>
    suspend fun getTotalIncomesByUser(userUid: String): Result<Double>
    suspend fun getBalanceByUser(userUid: String): Result<Double>
    suspend fun getExpensesByCategory(userUid: String): Result<Map<String, Double>>
    suspend fun getMonthlyExpensesByUser(userUid: String, year: Int, month: Int): Result<Double>
    suspend fun getMonthlyIncomesByUser(userUid: String, year: Int, month: Int): Result<Double>
}
