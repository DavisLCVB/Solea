package com.grupo03.solea.data.models

data class Expense(
    val id: String = "",
    val movementId: String = "",
    val sourceId: String = ""
)


/**
 * Source with complete related data (either item or receipt with its items)
 */
sealed class SourceDetails {
    data class ItemSource(
        val source: Source = Source(),
        val item: Item = Item()
    ) : SourceDetails()

    data class ReceiptSource(
        val source: Source = Source(),
        val receipt: Receipt = Receipt(),
        val items: List<Item> = emptyList()
    ) : SourceDetails()
}

/**
 * Complete expense information with all related data
 */
data class ExpenseDetails(
    val expense: Expense = Expense(),
    val movement: Movement = Movement(),
    val source: SourceDetails = SourceDetails.ItemSource()
)


/**
 * Helper to get the source object
 */
val SourceDetails.source: Source
    get() = when (this) {
        is SourceDetails.ItemSource -> source
        is SourceDetails.ReceiptSource -> source
    }

/**
 * Helper to get total amount from source
 */
val SourceDetails.totalAmount: Double
    get() = when (this) {
        is SourceDetails.ItemSource -> item.totalPrice
        is SourceDetails.ReceiptSource -> receipt.total
    }