package com.grupo03.solea.data.repositories.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
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
import com.grupo03.solea.data.models.SourceDetails
import com.grupo03.solea.data.models.SourceType
import com.grupo03.solea.data.repositories.interfaces.MovementRepository
import com.grupo03.solea.utils.MovementError
import com.grupo03.solea.utils.RepositoryResult
import com.grupo03.solea.utils.mapFirestoreException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

class FirebaseMovementRepository(
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : MovementRepository {

    private val movementsCollection = firestore.collection("movements")
    private val expensesCollection = firestore.collection("expenses")
    private val incomesCollection = firestore.collection("incomes")
    private val sourcesCollection = firestore.collection("sources")
    private val itemsCollection = firestore.collection("items")
    private val receiptsCollection = firestore.collection("receipts")

    override suspend fun createMovement(movement: Movement): RepositoryResult<Movement> {
        return try {
            val movementData = movement.toMap()
            movementsCollection.document(movement.id).set(movementData).await()
            RepositoryResult.Success(movement)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, MovementError.CREATION_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(MovementError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getMovementById(id: String): RepositoryResult<Movement?> {
        return try {
            val document = movementsCollection.document(id).get().await()
            val movement = document.toMovement()
            RepositoryResult.Success(movement)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, MovementError.FETCH_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(MovementError.UNKNOWN_ERROR)
        }
    }

    override suspend fun updateMovement(movement: Movement): RepositoryResult<Movement> {
        return try {
            val movementData = movement.toMap()
            movementsCollection.document(movement.id).set(movementData).await()
            RepositoryResult.Success(movement)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, MovementError.UPDATE_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(MovementError.UNKNOWN_ERROR)
        }
    }

    override suspend fun deleteMovement(id: String): RepositoryResult<Unit> {
        return try {
            movementsCollection.document(id).delete().await()
            RepositoryResult.Success(Unit)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, MovementError.DELETE_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(MovementError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getMovementsByUserId(userUid: String): RepositoryResult<List<Movement>> {
        return try {
            val documents = movementsCollection
                .whereEqualTo("userUid", userUid)
                .get()
                .await()

            val movements = documents.mapNotNull { it.toMovement() }
            RepositoryResult.Success(movements)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, MovementError.FETCH_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(MovementError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getMovementsByUserAndType(
        userUid: String,
        type: MovementType
    ): RepositoryResult<List<Movement>> {
        return try {
            val documents = movementsCollection
                .whereEqualTo("userUid", userUid)
                .whereEqualTo("type", type.name)
                .get()
                .await()

            val movements = documents.mapNotNull { it.toMovement() }
            RepositoryResult.Success(movements)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, MovementError.FETCH_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(MovementError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getMovementsByUserAndDateRange(
        userUid: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): RepositoryResult<List<Movement>> {
        return try {
            val startTimestamp = startDate.toEpochSecond(ZoneOffset.UTC)
            val endTimestamp = endDate.toEpochSecond(ZoneOffset.UTC)

            val documents = movementsCollection
                .whereEqualTo("userUid", userUid)
                .whereGreaterThanOrEqualTo("datetimeTimestamp", startTimestamp)
                .whereLessThanOrEqualTo("datetimeTimestamp", endTimestamp)
                .get()
                .await()

            val movements = documents.mapNotNull { it.toMovement() }
            RepositoryResult.Success(movements)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, MovementError.FETCH_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(MovementError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getMovementsByUserAndCategory(
        userUid: String,
        category: String
    ): RepositoryResult<List<Movement>> {
        return try {
            val documents = movementsCollection
                .whereEqualTo("userUid", userUid)
                .whereEqualTo("category", category)
                .get()
                .await()

            val movements = documents.mapNotNull { it.toMovement() }
            RepositoryResult.Success(movements)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, MovementError.FETCH_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(MovementError.UNKNOWN_ERROR)
        }
    }

    override suspend fun createExpense(
        movement: Movement,
        source: Source,
        item: Item?,
        receipt: Receipt?,
        receiptItems: List<Item>?
    ): RepositoryResult<ExpenseDetails> {
        return try {
            val sourceData = source.toMap()
            sourcesCollection.document(source.id).set(sourceData).await()

            when (source.sourceType) {
                SourceType.ITEM -> {
                    if (item == null) {
                        return RepositoryResult.Error(MovementError.CREATION_FAILED)
                    }
                    val itemData = item.toMap()
                    itemsCollection.document(item.id).set(itemData).await()
                }
                SourceType.RECEIPT -> {
                    if (receipt == null) {
                        return RepositoryResult.Error(MovementError.CREATION_FAILED)
                    }
                    val receiptData = receipt.toMap()
                    receiptsCollection.document(receipt.id).set(receiptData).await()

                    receiptItems?.forEach { receiptItem ->
                        val itemData = receiptItem.toMap()
                        itemsCollection.document(receiptItem.id).set(itemData).await()
                    }
                }
            }

            val movementData = movement.toMap()
            movementsCollection.document(movement.id).set(movementData).await()

            val expenseId = UUID.randomUUID().toString()
            val expense = Expense(
                id = expenseId,
                movementId = movement.id,
                sourceId = source.id
            )
            val expenseData = expense.toMap()
            expensesCollection.document(expenseId).set(expenseData).await()

            val sourceDetails = when (source.sourceType) {
                SourceType.ITEM -> {
                    SourceDetails.ItemSource(
                        source = source,
                        item = item!!
                    )
                }
                SourceType.RECEIPT -> {
                    SourceDetails.ReceiptSource(
                        source = source,
                        receipt = receipt!!,
                        items = receiptItems ?: emptyList()
                    )
                }
            }

            RepositoryResult.Success(
                ExpenseDetails(
                    expense = expense,
                    movement = movement,
                    source = sourceDetails
                )
            )
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, MovementError.CREATION_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(MovementError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getExpenseById(id: String): RepositoryResult<ExpenseDetails?> {
        return try {
            val expenseDoc = expensesCollection.document(id).get().await()
            val expense = expenseDoc.toExpense() ?: return RepositoryResult.Success(null)

            val movementDoc = movementsCollection.document(expense.movementId).get().await()
            val movement = movementDoc.toMovement() ?: return RepositoryResult.Success(null)

            val sourceDoc = sourcesCollection.document(expense.sourceId).get().await()
            val source = sourceDoc.toSource() ?: return RepositoryResult.Success(null)

            val sourceDetails = when (source.sourceType) {
                SourceType.ITEM -> {
                    val itemId = source.sourceItemId ?: return RepositoryResult.Success(null)
                    val itemDoc = itemsCollection.document(itemId).get().await()
                    val item = itemDoc.toItem() ?: return RepositoryResult.Success(null)
                    SourceDetails.ItemSource(
                        source = source,
                        item = item
                    )
                }
                SourceType.RECEIPT -> {
                    val receiptId = source.sourceReceiptId ?: return RepositoryResult.Success(null)
                    val receiptDoc = receiptsCollection.document(receiptId).get().await()
                    val receipt = receiptDoc.toReceipt() ?: return RepositoryResult.Success(null)

                    val itemsDocs = itemsCollection
                        .whereEqualTo("receiptId", receiptId)
                        .get()
                        .await()
                    val items = itemsDocs.mapNotNull { it.toItem() }

                    SourceDetails.ReceiptSource(
                        source = source,
                        receipt = receipt,
                        items = items
                    )
                }
            }

            RepositoryResult.Success(
                ExpenseDetails(
                    expense = expense,
                    movement = movement,
                    source = sourceDetails
                )
            )
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, MovementError.FETCH_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(MovementError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getExpensesByUserId(userUid: String): RepositoryResult<List<ExpenseDetails>> {
        return try {
            val movementDocs = movementsCollection
                .whereEqualTo("userUid", userUid)
                .whereEqualTo("type", MovementType.EXPENSE.name)
                .get()
                .await()

            val expenseDetails = mutableListOf<ExpenseDetails>()

            for (movementDoc in movementDocs) {
                val movement = movementDoc.toMovement() ?: continue

                val expenseDocs = expensesCollection
                    .whereEqualTo("movementId", movement.id)
                    .get()
                    .await()

                val expense = expenseDocs.firstOrNull()?.toExpense() ?: continue

                val sourceDoc = sourcesCollection.document(expense.sourceId).get().await()
                val source = sourceDoc.toSource() ?: continue

                val sourceDetails = when (source.sourceType) {
                    SourceType.ITEM -> {
                        val itemId = source.sourceItemId ?: continue
                        val itemDoc = itemsCollection.document(itemId).get().await()
                        val item = itemDoc.toItem() ?: continue
                        SourceDetails.ItemSource(
                            source = source,
                            item = item
                        )
                    }
                    SourceType.RECEIPT -> {
                        val receiptId = source.sourceReceiptId ?: continue
                        val receiptDoc = receiptsCollection.document(receiptId).get().await()
                        val receipt = receiptDoc.toReceipt() ?: continue

                        val itemsDocs = itemsCollection
                            .whereEqualTo("receiptId", receiptId)
                            .get()
                            .await()
                        val items = itemsDocs.mapNotNull { it.toItem() }

                        SourceDetails.ReceiptSource(
                            source = source,
                            receipt = receipt,
                            items = items
                        )
                    }
                }

                expenseDetails.add(
                    ExpenseDetails(
                        expense = expense,
                        movement = movement,
                        source = sourceDetails
                    )
                )
            }

            RepositoryResult.Success(expenseDetails)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, MovementError.FETCH_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(MovementError.UNKNOWN_ERROR)
        }
    }

    override suspend fun createIncome(movement: Movement): RepositoryResult<IncomeDetails> {
        return try {
            val movementData = movement.toMap()
            movementsCollection.document(movement.id).set(movementData).await()

            val incomeId = UUID.randomUUID().toString()
            val income = Income(
                id = incomeId,
                movementId = movement.id
            )
            val incomeData = income.toMap()
            incomesCollection.document(incomeId).set(incomeData).await()

            RepositoryResult.Success(
                IncomeDetails(
                    income = income,
                    movement = movement
                )
            )
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, MovementError.CREATION_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(MovementError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getIncomeById(id: String): RepositoryResult<IncomeDetails?> {
        return try {
            val incomeDoc = incomesCollection.document(id).get().await()
            val income = incomeDoc.toIncome() ?: return RepositoryResult.Success(null)

            val movementDoc = movementsCollection.document(income.movementId).get().await()
            val movement = movementDoc.toMovement() ?: return RepositoryResult.Success(null)

            RepositoryResult.Success(
                IncomeDetails(
                    income = income,
                    movement = movement
                )
            )
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, MovementError.FETCH_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(MovementError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getIncomesByUserId(userUid: String): RepositoryResult<List<IncomeDetails>> {
        return try {
            val movementDocs = movementsCollection
                .whereEqualTo("userUid", userUid)
                .whereEqualTo("type", MovementType.INCOME.name)
                .get()
                .await()

            val incomeDetails = mutableListOf<IncomeDetails>()

            for (movementDoc in movementDocs) {
                val movement = movementDoc.toMovement() ?: continue

                val incomeDocs = incomesCollection
                    .whereEqualTo("movementId", movement.id)
                    .get()
                    .await()

                val income = incomeDocs.firstOrNull()?.toIncome() ?: continue

                incomeDetails.add(
                    IncomeDetails(
                        income = income,
                        movement = movement
                    )
                )
            }

            RepositoryResult.Success(incomeDetails)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, MovementError.FETCH_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(MovementError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getTotalExpensesByUser(userUid: String): RepositoryResult<Double> {
        return try {
            val movementsResult = getMovementsByUserAndType(userUid, MovementType.EXPENSE)
            if (movementsResult is RepositoryResult.Error) {
                return movementsResult
            }
            val movements = (movementsResult as RepositoryResult.Success).data
            val total = movements.sumOf { it.total }
            RepositoryResult.Success(total)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, MovementError.FETCH_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(MovementError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getTotalIncomesByUser(userUid: String): RepositoryResult<Double> {
        return try {
            val movementsResult = getMovementsByUserAndType(userUid, MovementType.INCOME)
            if (movementsResult is RepositoryResult.Error) {
                return movementsResult
            }
            val movements = (movementsResult as RepositoryResult.Success).data
            val total = movements.sumOf { it.total }
            RepositoryResult.Success(total)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, MovementError.FETCH_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(MovementError.UNKNOWN_ERROR)
        }
    }

    private val savingsCollection = firestore.collection("savings")

    override suspend fun createSaving(
        movement: Movement,
        save: Save
    ): RepositoryResult<SaveDetails> {
        return try {
            val movementData = movement.toMap()
            movementsCollection.document(movement.id).set(movementData).await()

            val saveId = save.id.ifEmpty { UUID.randomUUID().toString() }
            val saving = save.copy(id = saveId, movementId = movement.id)
            val saveData = saving.toMap()
            savingsCollection.document(saveId).set(saveData).await()

            RepositoryResult.Success(
                SaveDetails(
                    save = saving,
                    movement = movement
                )
            )
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, MovementError.CREATION_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(MovementError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getSavingById(id: String): RepositoryResult<SaveDetails?> {
        return try {
            val saveDoc = savingsCollection.document(id).get().await()
            val save = saveDoc.toSave() ?: return RepositoryResult.Success(null)

            val movementDoc = movementsCollection.document(save.movementId).get().await()
            val movement = movementDoc.toMovement() ?: return RepositoryResult.Success(null)

            RepositoryResult.Success(
                SaveDetails(
                    save = save,
                    movement = movement
                )
            )
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, MovementError.FETCH_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(MovementError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getSavingsByUserId(userUid: String): RepositoryResult<List<SaveDetails>> {
        return try {
            val movementDocs = movementsCollection
                .whereEqualTo("userUid", userUid)
                .whereEqualTo("type", MovementType.SAVING.name)
                .get()
                .await()

            val saveDetails = mutableListOf<SaveDetails>()

            for (movementDoc in movementDocs) {
                val movement = movementDoc.toMovement() ?: continue

                val savingDocs = savingsCollection
                    .whereEqualTo("movementId", movement.id)
                    .get()
                    .await()

                val saving = savingDocs.firstOrNull()?.toSave() ?: continue

                saveDetails.add(
                    SaveDetails(
                        save = saving,
                        movement = movement
                    )
                )
            }

            RepositoryResult.Success(saveDetails)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, MovementError.FETCH_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(MovementError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getSavingsByGoalId(goalId: String): RepositoryResult<List<SaveDetails>> {
        return try {
            val savingDocs = savingsCollection
                .whereEqualTo("goalId", goalId)
                .get()
                .await()

            val saveDetails = mutableListOf<SaveDetails>()

            for (savingDoc in savingDocs) {
                val saving = savingDoc.toSave() ?: continue

                val movementDoc = movementsCollection.document(saving.movementId).get().await()
                val movement = movementDoc.toMovement() ?: continue

                saveDetails.add(
                    SaveDetails(
                        save = saving,
                        movement = movement
                    )
                )
            }

            RepositoryResult.Success(saveDetails)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, MovementError.FETCH_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(MovementError.UNKNOWN_ERROR)
        }
    }

    override suspend fun deleteSavingsByGoalId(goalId: String): RepositoryResult<Unit> {
        return try {
            val savingsResult = getSavingsByGoalId(goalId)
            if (savingsResult.isError) {
                return savingsResult as RepositoryResult.Error
            }

            val savings = (savingsResult as RepositoryResult.Success).data

            for (saveDetail in savings) {
                movementsCollection.document(saveDetail.movement.id).delete().await()
                savingsCollection.document(saveDetail.save.id).delete().await()
            }

            RepositoryResult.Success(Unit)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, MovementError.DELETE_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(MovementError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getBalanceByUser(userUid: String): RepositoryResult<Double> {
        return try {
            val totalIncomesResult = getTotalIncomesByUser(userUid)
            if (totalIncomesResult is RepositoryResult.Error) return totalIncomesResult
            val totalIncomes = (totalIncomesResult as RepositoryResult.Success).data

            val totalExpensesResult = getTotalExpensesByUser(userUid)
            if (totalExpensesResult is RepositoryResult.Error) return totalExpensesResult
            val totalExpenses = (totalExpensesResult as RepositoryResult.Success).data

            val savingsResult = getSavingsByUserId(userUid)
            val totalSavings = if (savingsResult is RepositoryResult.Success)
                savingsResult.data.sumOf { it.movement.total } else 0.0

            val balance = totalIncomes - totalExpenses - totalSavings
            RepositoryResult.Success(balance)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, MovementError.FETCH_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(MovementError.UNKNOWN_ERROR)
        }
    }


    override suspend fun getExpensesByCategory(userUid: String): RepositoryResult<Map<String, Double>> {
        return try {
            val movementsResult = getMovementsByUserAndType(userUid, MovementType.EXPENSE)
            if (movementsResult is RepositoryResult.Error) {
                return movementsResult
            }
            val movements = (movementsResult as RepositoryResult.Success).data
            val expensesByCategory = movements
                .filter { it.category != null }
                .groupBy { it.category!! }
                .mapValues { (_, movements) -> movements.sumOf { it.total } }
            RepositoryResult.Success(expensesByCategory)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, MovementError.FETCH_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(MovementError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getMonthlyExpensesByUser(
        userUid: String,
        year: Int,
        month: Int
    ): RepositoryResult<Double> {
        return try {
            val startDate = LocalDateTime.of(year, month, 1, 0, 0)
            val endDate = startDate.plusMonths(1).minusSeconds(1)

            val movementsResult = getMovementsByUserAndDateRange(userUid, startDate, endDate)
            if (movementsResult is RepositoryResult.Error) {
                return movementsResult
            }
            val movements = (movementsResult as RepositoryResult.Success).data
            val expenses = movements.filter { it.type == MovementType.EXPENSE }
            val total = expenses.sumOf { it.total }

            RepositoryResult.Success(total)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, MovementError.FETCH_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(MovementError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getMonthlyIncomesByUser(
        userUid: String,
        year: Int,
        month: Int
    ): RepositoryResult<Double> {
        return try {
            val startDate = LocalDateTime.of(year, month, 1, 0, 0)
            val endDate = startDate.plusMonths(1).minusSeconds(1)

            val movementsResult = getMovementsByUserAndDateRange(userUid, startDate, endDate)
            if (movementsResult is RepositoryResult.Error) {
                return movementsResult
            }
            val movements = (movementsResult as RepositoryResult.Success).data
            val incomes = movements.filter { it.type == MovementType.INCOME }
            val total = incomes.sumOf { it.total }

            RepositoryResult.Success(total)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, MovementError.FETCH_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(MovementError.UNKNOWN_ERROR)
        }
    }

    override suspend fun createIncome(income: Income): RepositoryResult<Income> {
        return try {
            val incomeData = income.toMap()
            incomesCollection.document(income.id).set(incomeData).await()
            RepositoryResult.Success(income)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, MovementError.CREATION_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(MovementError.UNKNOWN_ERROR)
        }
    }

    override suspend fun createExpense(expense: Expense): RepositoryResult<Expense> {
        return try {
            val expenseData = expense.toMap()
            expensesCollection.document(expense.id).set(expenseData).await()
            RepositoryResult.Success(expense)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, MovementError.CREATION_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(MovementError.UNKNOWN_ERROR)
        }
    }

    override suspend fun createSource(source: Source): RepositoryResult<Source> {
        return try {
            val sourceData = source.toMap()
            sourcesCollection.document(source.id).set(sourceData).await()
            RepositoryResult.Success(source)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, MovementError.CREATION_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(MovementError.UNKNOWN_ERROR)
        }
    }

    private fun Movement.toMap(): Map<String, Any?> {
        return buildMap {
            put("id", id)
            put("userUid", userUid)
            put("type", type.name)
            put("name", name)
            put("description", description)
            put("datetimeTimestamp", datetime.toEpochSecond(ZoneOffset.UTC))
            put("currency", currency)
            put("total", total)
            category?.let { put("category", it) }
            put("createdAtTimestamp", createdAt.toEpochSecond(ZoneOffset.UTC))
        }
    }

    private fun Source.toMap(): Map<String, Any> {
        return buildMap {
            put("id", id)
            put("sourceType", sourceType.name)
            sourceItemId?.let { put("sourceItemId", it) }
            sourceReceiptId?.let { put("sourceReceiptId", it) }
            put("createdAtTimestamp", createdAt.toEpochSecond(ZoneOffset.UTC))
        }
    }

    private fun Expense.toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "movementId" to movementId,
            "sourceId" to sourceId
        )
    }

    private fun Income.toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "movementId" to movementId
        )
    }
    private fun Save.toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "movementId" to movementId,
            "goalId" to goalId,
            "amount" to amount
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toSave(): Save? {
        return try {
            Save(
                id = getString("id") ?: return null,
                movementId = getString("movementId") ?: "",
                goalId = getString("goalId") ?: "",
                amount = getDouble("amount") ?: 0.0
            )
        } catch (_: Exception) {
            null
        }
    }
    private fun com.google.firebase.firestore.DocumentSnapshot.toMovement(): Movement? {
        return try {
            Movement(
                id = getString("id") ?: return null,
                userUid = getString("userUid") ?: "",
                type = MovementType.valueOf(getString("type") ?: "EXPENSE"),
                name = getString("name") ?: "",
                description = getString("description") ?: "",
                datetime = LocalDateTime.ofEpochSecond(
                    getLong("datetimeTimestamp") ?: 0,
                    0,
                    ZoneOffset.UTC
                ),
                currency = getString("currency") ?: "ARS",
                total = getDouble("total") ?: 0.0,
                category = getString("category"),
                createdAt = LocalDateTime.ofEpochSecond(
                    getLong("createdAtTimestamp") ?: 0,
                    0,
                    ZoneOffset.UTC
                )
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toSource(): Source? {
        return try {
            Source(
                id = getString("id") ?: return null,
                sourceType = SourceType.valueOf(getString("sourceType") ?: "RECEIPT"),
                sourceItemId = getString("sourceItemId"),
                sourceReceiptId = getString("sourceReceiptId"),
                createdAt = LocalDateTime.ofEpochSecond(
                    getLong("createdAtTimestamp") ?: 0,
                    0,
                    ZoneOffset.UTC
                )
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toExpense(): Expense? {
        return try {
            Expense(
                id = getString("id") ?: return null,
                movementId = getString("movementId") ?: "",
                sourceId = getString("sourceId") ?: ""
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toIncome(): Income? {
        return try {
            Income(
                id = getString("id") ?: return null,
                movementId = getString("movementId") ?: ""
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toItem(): Item? {
        return try {
            Item(
                id = getString("id") ?: return null,
                receiptId = getString("receiptId") ?: "",
                description = getString("description") ?: "",
                quantity = getDouble("quantity") ?: 0.0,
                currency = getString("currency") ?: "ARS",
                unitPrice = getDouble("unitPrice") ?: 0.0,
                totalPrice = getDouble("totalPrice") ?: 0.0,
                category = getString("category") ?: "",
                createdAt = LocalDateTime.ofEpochSecond(
                    getLong("createdAtTimestamp") ?: 0,
                    0,
                    ZoneOffset.UTC
                )
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toReceipt(): Receipt? {
        return try {
            Receipt(
                id = getString("id") ?: return null,
                description = getString("description") ?: "",
                datetime = LocalDateTime.ofEpochSecond(
                    getLong("datetimeTimestamp") ?: 0,
                    0,
                    ZoneOffset.UTC
                ),
                currency = getString("currency") ?: "ARS",
                total = getDouble("total") ?: 0.0,
                createdAt = LocalDateTime.ofEpochSecond(
                    getLong("createdAtTimestamp") ?: 0,
                    0,
                    ZoneOffset.UTC
                )
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun Item.toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "receiptId" to receiptId,
            "description" to description,
            "quantity" to quantity,
            "currency" to currency,
            "unitPrice" to unitPrice,
            "totalPrice" to totalPrice,
            "category" to category,
            "createdAtTimestamp" to createdAt.toEpochSecond(ZoneOffset.UTC),
        )
    }

    private fun Receipt.toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "description" to description,
            "datetimeTimestamp" to datetime.toEpochSecond(ZoneOffset.UTC),
            "currency" to currency,
            "total" to total,
            "createdAtTimestamp" to createdAt.toEpochSecond(ZoneOffset.UTC)
        )
    }

    override fun observeIncomesByUserId(userUid: String): Flow<RepositoryResult<List<IncomeDetails>>> = callbackFlow {
        val listener = movementsCollection
            .whereEqualTo("userUid", userUid)
            .whereEqualTo("type", MovementType.INCOME.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(RepositoryResult.Error(mapFirestoreException(error as FirebaseFirestoreException, MovementError.FETCH_FAILED)))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val incomeDetails = mutableListOf<IncomeDetails>()

                    for (movementDoc in snapshot.documents) {
                        val movement = movementDoc.toMovement() ?: continue

                        incomesCollection
                            .whereEqualTo("movementId", movement.id)
                            .get()
                            .addOnSuccessListener { incomeDocs ->
                                val income = incomeDocs.firstOrNull()?.toIncome() ?: return@addOnSuccessListener

                                incomeDetails.add(
                                    IncomeDetails(
                                        income = income,
                                        movement = movement
                                    )
                                )

                                if (incomeDetails.size == snapshot.documents.size) {
                                    trySend(RepositoryResult.Success(incomeDetails))
                                }
                            }
                    }

                    if (snapshot.documents.isEmpty()) {
                        trySend(RepositoryResult.Success(emptyList()))
                    }
                }
            }

        awaitClose { listener.remove() }
    }

    override fun observeExpensesByUserId(userUid: String): Flow<RepositoryResult<List<ExpenseDetails>>> = callbackFlow {
        val listener = movementsCollection
            .whereEqualTo("userUid", userUid)
            .whereEqualTo("type", MovementType.EXPENSE.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(RepositoryResult.Error(mapFirestoreException(error as FirebaseFirestoreException, MovementError.FETCH_FAILED)))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val expenseDetails = mutableListOf<ExpenseDetails>()
                    var processedCount = 0

                    if (snapshot.documents.isEmpty()) {
                        trySend(RepositoryResult.Success(emptyList()))
                        return@addSnapshotListener
                    }

                    for (movementDoc in snapshot.documents) {
                        val movement = movementDoc.toMovement()
                        if (movement == null) {
                            processedCount++
                            if (processedCount == snapshot.documents.size) {
                                trySend(RepositoryResult.Success(expenseDetails))
                            }
                        } else {
                            // Get expense by movement ID
                            expensesCollection
                            .whereEqualTo("movementId", movement.id)
                            .get()
                            .addOnSuccessListener { expenseDocs ->
                                val expense = expenseDocs.firstOrNull()?.toExpense()
                                if (expense == null) {
                                    processedCount++
                                    if (processedCount == snapshot.documents.size) {
                                        trySend(RepositoryResult.Success(expenseDetails))
                                    }
                                    return@addOnSuccessListener
                                }

                                sourcesCollection.document(expense.sourceId).get()
                                    .addOnSuccessListener { sourceDoc ->
                                        val source = sourceDoc.toSource()
                                        if (source == null) {
                                            processedCount++
                                            if (processedCount == snapshot.documents.size) {
                                                trySend(RepositoryResult.Success(expenseDetails))
                                            }
                                            return@addOnSuccessListener
                                        }

                                        when (source.sourceType) {
                                            SourceType.ITEM -> {
                                                val itemId = source.sourceItemId
                                                if (itemId == null) {
                                                    processedCount++
                                                    if (processedCount == snapshot.documents.size) {
                                                        trySend(RepositoryResult.Success(expenseDetails))
                                                    }
                                                    return@addOnSuccessListener
                                                }

                                                itemsCollection.document(itemId).get()
                                                    .addOnSuccessListener { itemDoc ->
                                                        val item = itemDoc.toItem()
                                                        if (item != null) {
                                                            expenseDetails.add(
                                                                ExpenseDetails(
                                                                    expense = expense,
                                                                    movement = movement,
                                                                    source = SourceDetails.ItemSource(source, item)
                                                                )
                                                            )
                                                        }
                                                        processedCount++
                                                        if (processedCount == snapshot.documents.size) {
                                                            trySend(RepositoryResult.Success(expenseDetails))
                                                        }
                                                    }
                                            }
                                            SourceType.RECEIPT -> {
                                                val receiptId = source.sourceReceiptId
                                                if (receiptId == null) {
                                                    processedCount++
                                                    if (processedCount == snapshot.documents.size) {
                                                        trySend(RepositoryResult.Success(expenseDetails))
                                                    }
                                                    return@addOnSuccessListener
                                                }

                                                receiptsCollection.document(receiptId).get()
                                                    .addOnSuccessListener { receiptDoc ->
                                                        val receipt = receiptDoc.toReceipt()
                                                        if (receipt == null) {
                                                            processedCount++
                                                            if (processedCount == snapshot.documents.size) {
                                                                trySend(RepositoryResult.Success(expenseDetails))
                                                            }
                                                            return@addOnSuccessListener
                                                        }

                                                        itemsCollection
                                                            .whereEqualTo("receiptId", receiptId)
                                                            .get()
                                                            .addOnSuccessListener { itemsDocs ->
                                                                val items = itemsDocs.mapNotNull { it.toItem() }
                                                                expenseDetails.add(
                                                                    ExpenseDetails(
                                                                        expense = expense,
                                                                        movement = movement,
                                                                        source = SourceDetails.ReceiptSource(source, receipt, items)
                                                                    )
                                                                )
                                                                processedCount++
                                                                if (processedCount == snapshot.documents.size) {
                                                                    trySend(RepositoryResult.Success(expenseDetails))
                                                                }
                                                            }
                                                    }
                                            }
                                        }
                                    }
                            }
                        }
                    }
                }
            }

        awaitClose { listener.remove() }
    }

    override fun observeSavingsByUserId(userUid: String): Flow<RepositoryResult<List<SaveDetails>>> = callbackFlow {
        val listener = movementsCollection
            .whereEqualTo("userUid", userUid)
            .whereEqualTo("type", MovementType.SAVING.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(
                        RepositoryResult.Error(
                            mapFirestoreException(error as FirebaseFirestoreException, MovementError.FETCH_FAILED)
                        )
                    )
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val saveDetails = mutableListOf<SaveDetails>()
                    var processedCount = 0

                    if (snapshot.documents.isEmpty()) {
                        trySend(RepositoryResult.Success(emptyList()))
                        return@addSnapshotListener
                    }

                    for (movementDoc in snapshot.documents) {
                        val movement = movementDoc.toMovement() ?: continue

                        savingsCollection
                            .whereEqualTo("movementId", movement.id)
                            .get()
                            .addOnSuccessListener { saveDocs ->
                                val saving = saveDocs.firstOrNull()?.toSave()
                                if (saving != null) {
                                    saveDetails.add(SaveDetails(save = saving, movement = movement))
                                }
                                processedCount++
                                if (processedCount == snapshot.documents.size) {
                                    trySend(RepositoryResult.Success(saveDetails))
                                }
                            }
                            .addOnFailureListener {
                                processedCount++
                                if (processedCount == snapshot.documents.size) {
                                    trySend(RepositoryResult.Success(saveDetails))
                                }
                            }
                    }
                }
            }

        awaitClose { listener.remove() }
    }
}
