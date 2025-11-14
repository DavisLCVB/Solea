package com.grupo03.solea.data.repositories

import com.grupo03.solea.data.local.MovementDao
import com.grupo03.solea.data.local.toEntity
import com.grupo03.solea.data.local.toModel
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
import com.grupo03.solea.data.repositories.interfaces.MovementRepository
import com.grupo03.solea.utils.RepositoryResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * Implementation of MovementRepository that uses Room as a local cache.
 *
 * This repository wraps a remote repository (e.g., Firebase) and provides:
 * - Fast local access via Room database
 * - Offline capability
 * - Reduced network calls
 *
 */
class CachedMovementRepository(
    private val remoteRepository: MovementRepository,
    private val movementDao: MovementDao
) : MovementRepository {

    override suspend fun createMovement(movement: Movement): RepositoryResult<Movement> {
        val result = remoteRepository.createMovement(movement)
        if (result is RepositoryResult.Success) {
            movementDao.insertMovement(movement.toEntity())
        }
        return result
    }

    override suspend fun getMovementById(id: String): RepositoryResult<Movement?> {
        val cachedMovement = movementDao.getMovementById(id)?.toModel()
        if (cachedMovement != null) {
            return RepositoryResult.Success(cachedMovement)
        }

        val result = remoteRepository.getMovementById(id)
        if (result is RepositoryResult.Success && result.data != null) {
            movementDao.insertMovement(result.data.toEntity())
        }
        return result
    }

    override suspend fun updateMovement(movement: Movement): RepositoryResult<Movement> {
        val result = remoteRepository.updateMovement(movement)
        if (result is RepositoryResult.Success) {
            movementDao.insertMovement(movement.toEntity())
        }
        return result
    }

    override suspend fun deleteMovement(id: String): RepositoryResult<Unit> {
        val result = remoteRepository.deleteMovement(id)
        if (result is RepositoryResult.Success) {
            movementDao.deleteMovement(id)
        }
        return result
    }

    override suspend fun getMovementsByUserId(userUid: String): RepositoryResult<List<Movement>> {
        val cachedMovements = movementDao.getMovementsByUser(userUid).map { it.toModel() }
        if (cachedMovements.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                val remoteResult = remoteRepository.getMovementsByUserId(userUid)
                if (remoteResult is RepositoryResult.Success) {
                    movementDao.insertMovements(remoteResult.data.map { it.toEntity() })
                }
            }
            return RepositoryResult.Success(cachedMovements)
        }

        val result = remoteRepository.getMovementsByUserId(userUid)
        if (result is RepositoryResult.Success) {
            movementDao.insertMovements(result.data.map { it.toEntity() })
        }
        return result
    }

    override suspend fun getMovementsByUserAndType(
        userUid: String,
        type: MovementType
    ): RepositoryResult<List<Movement>> {
        val cachedMovements = movementDao.getMovementsByUserAndType(userUid, type).map { it.toModel() }
        if (cachedMovements.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                val remoteResult = remoteRepository.getMovementsByUserAndType(userUid, type)
                if (remoteResult is RepositoryResult.Success) {
                    movementDao.insertMovements(remoteResult.data.map { it.toEntity() })
                }
            }
            return RepositoryResult.Success(cachedMovements)
        }

        val result = remoteRepository.getMovementsByUserAndType(userUid, type)
        if (result is RepositoryResult.Success) {
            movementDao.insertMovements(result.data.map { it.toEntity() })
        }
        return result
    }

    override suspend fun getMovementsByUserAndDateRange(
        userUid: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): RepositoryResult<List<Movement>> {
        val cachedMovements = movementDao.getMovementsByUserAndDateRange(userUid, startDate, endDate)
            .map { it.toModel() }

        if (cachedMovements.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                val remoteResult = remoteRepository.getMovementsByUserAndDateRange(userUid, startDate, endDate)
                if (remoteResult is RepositoryResult.Success) {
                    movementDao.insertMovements(remoteResult.data.map { it.toEntity() })
                }
            }
            return RepositoryResult.Success(cachedMovements)
        }

        val remoteResult = remoteRepository.getMovementsByUserAndDateRange(userUid, startDate, endDate)
        if (remoteResult is RepositoryResult.Success) {
            movementDao.insertMovements(remoteResult.data.map { it.toEntity() })
        }
        return remoteResult
    }

    override suspend fun getMovementsByUserAndCategory(
        userUid: String,
        category: String
    ): RepositoryResult<List<Movement>> {
        val result = remoteRepository.getMovementsByUserAndCategory(userUid, category)
        if (result is RepositoryResult.Success) {
            movementDao.insertMovements(result.data.map { it.toEntity() })
        }
        return result
    }

    override suspend fun createExpense(
        movement: Movement,
        source: Source,
        item: Item?,
        receipt: Receipt?,
        receiptItems: List<Item>?
    ): RepositoryResult<ExpenseDetails> {
        val result = remoteRepository.createExpense(movement, source, item, receipt, receiptItems)
        if (result is RepositoryResult.Success) {
            movementDao.insertMovement(result.data.movement.toEntity())
        }
        return result
    }

    override suspend fun getExpenseById(id: String): RepositoryResult<ExpenseDetails?> {
        return remoteRepository.getExpenseById(id)
    }

    override suspend fun getExpensesByUserId(userUid: String): RepositoryResult<List<ExpenseDetails>> {
        return remoteRepository.getExpensesByUserId(userUid)
    }

    override suspend fun createIncome(movement: Movement): RepositoryResult<IncomeDetails> {
        val result = remoteRepository.createIncome(movement)
        if (result is RepositoryResult.Success) {
            movementDao.insertMovement(result.data.movement.toEntity())
        }
        return result
    }

    override suspend fun createIncome(income: Income): RepositoryResult<Income> {
        return remoteRepository.createIncome(income)
    }

    override suspend fun getIncomeById(id: String): RepositoryResult<IncomeDetails?> {
        return remoteRepository.getIncomeById(id)
    }

    override suspend fun getIncomesByUserId(userUid: String): RepositoryResult<List<IncomeDetails>> {
        return remoteRepository.getIncomesByUserId(userUid)
    }

    override suspend fun createSaving(movement: Movement, save: Save): RepositoryResult<SaveDetails> {
        val result = remoteRepository.createSaving(movement, save)
        if (result is RepositoryResult.Success) {
            movementDao.insertMovement(result.data.movement.toEntity())
        }
        return result
    }

    override suspend fun getSavingById(id: String): RepositoryResult<SaveDetails?> {
        return remoteRepository.getSavingById(id)
    }

    override suspend fun getSavingsByUserId(userUid: String): RepositoryResult<List<SaveDetails>> {
        return remoteRepository.getSavingsByUserId(userUid)
    }

    override suspend fun getSavingsByGoalId(goalId: String): RepositoryResult<List<SaveDetails>> {
        return remoteRepository.getSavingsByGoalId(goalId)
    }

    override suspend fun deleteSavingsByGoalId(goalId: String): RepositoryResult<Unit> {
        val result = remoteRepository.deleteSavingsByGoalId(goalId)
        if (result is RepositoryResult.Success) {
        }
        return result
    }

    override suspend fun createExpense(expense: Expense): RepositoryResult<Expense> {
        return remoteRepository.createExpense(expense)
    }

    override suspend fun createSource(source: Source): RepositoryResult<Source> {
        return remoteRepository.createSource(source)
    }

    override suspend fun getTotalExpensesByUser(userUid: String): RepositoryResult<Double> {
        return remoteRepository.getTotalExpensesByUser(userUid)
    }

    override suspend fun getTotalIncomesByUser(userUid: String): RepositoryResult<Double> {
        return remoteRepository.getTotalIncomesByUser(userUid)
    }

    override suspend fun getBalanceByUser(userUid: String): RepositoryResult<Double> {
        return remoteRepository.getBalanceByUser(userUid)
    }

    override suspend fun getExpensesByCategory(userUid: String): RepositoryResult<Map<String, Double>> {
        return remoteRepository.getExpensesByCategory(userUid)
    }

    override suspend fun getMonthlyExpensesByUser(
        userUid: String,
        year: Int,
        month: Int
    ): RepositoryResult<Double> {
        return remoteRepository.getMonthlyExpensesByUser(userUid, year, month)
    }

    override suspend fun getMonthlyIncomesByUser(
        userUid: String,
        year: Int,
        month: Int
    ): RepositoryResult<Double> {
        return remoteRepository.getMonthlyIncomesByUser(userUid, year, month)
    }

    override fun observeIncomesByUserId(userUid: String): Flow<RepositoryResult<List<IncomeDetails>>> {
        CoroutineScope(Dispatchers.IO).launch {
            remoteRepository.observeIncomesByUserId(userUid).collect { result ->
                if (result is RepositoryResult.Success) {
                    movementDao.insertMovements(result.data.map { it.movement.toEntity() })
                }
            }
        }
        return remoteRepository.observeIncomesByUserId(userUid)
    }

    override fun observeExpensesByUserId(userUid: String): Flow<RepositoryResult<List<ExpenseDetails>>> {
        CoroutineScope(Dispatchers.IO).launch {
            remoteRepository.observeExpensesByUserId(userUid).collect { result ->
                if (result is RepositoryResult.Success) {
                    movementDao.insertMovements(result.data.map { it.movement.toEntity() })
                }
            }
        }
        return remoteRepository.observeExpensesByUserId(userUid)
    }

    override fun observeSavingsByUserId(userUid: String): Flow<RepositoryResult<List<SaveDetails>>> {
        CoroutineScope(Dispatchers.IO).launch {
            remoteRepository.observeSavingsByUserId(userUid).collect { result ->
                if (result is RepositoryResult.Success) {
                    movementDao.insertMovements(result.data.map { it.movement.toEntity() })
                }
            }
        }
        return remoteRepository.observeSavingsByUserId(userUid)
    }
}
