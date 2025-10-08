package com.grupo03.solea.data.models

/**
 * Represents an expense transaction.
 *
 * An Expense is a specific type of Movement that tracks financial outflows.
 * It is linked to a Source which can be either an individual Item or a Receipt containing multiple items.
 *
 * @property id Unique identifier for the expense
 * @property movementId ID of the associated Movement (must be of type EXPENSE)
 * @property sourceId ID of the Source that details where this expense came from
 */
data class Expense(
    val id: String = "",
    val movementId: String = "",
    val sourceId: String = ""
)


/**
 * Sealed class representing the complete details of an expense source.
 *
 * A source can be either an individual item or a receipt with multiple items.
 */
sealed class SourceDetails {
    /**
     * Source details for a single item expense.
     *
     * @property source The source reference
     * @property item The item details
     */
    data class ItemSource(
        val source: Source = Source(),
        val item: Item = Item()
    ) : SourceDetails()

    /**
     * Source details for a receipt-based expense.
     *
     * @property source The source reference
     * @property receipt The receipt details
     * @property items List of items contained in the receipt
     */
    data class ReceiptSource(
        val source: Source = Source(),
        val receipt: Receipt = Receipt(),
        val items: List<Item> = emptyList()
    ) : SourceDetails()
}

/**
 * Complete expense information with all related data.
 *
 * This aggregates an Expense with its Movement and Source details,
 * providing a complete view of the expense transaction.
 *
 * @property expense The expense record
 * @property movement The associated movement record
 * @property source The complete source details (item or receipt)
 */
data class ExpenseDetails(
    val expense: Expense = Expense(),
    val movement: Movement = Movement(),
    val source: SourceDetails = SourceDetails.ItemSource()
)


/**
 * Extension property to get the source object from SourceDetails.
 */
val SourceDetails.source: Source
    get() = when (this) {
        is SourceDetails.ItemSource -> source
        is SourceDetails.ReceiptSource -> source
    }

/**
 * Extension property to get the total amount from SourceDetails.
 */
val SourceDetails.totalAmount: Double
    get() = when (this) {
        is SourceDetails.ItemSource -> item.totalPrice
        is SourceDetails.ReceiptSource -> receipt.total
    }
