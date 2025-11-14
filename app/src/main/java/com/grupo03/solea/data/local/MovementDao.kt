package com.grupo03.solea.data.local

import androidx.room.*
import com.grupo03.solea.data.models.MovementType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Data Access Object for Movement operations in Room database.
 * Provides methods for caching and retrieving movements locally.
 */
@Dao
interface MovementDao {

    /**
     * Inserts or replaces a movement in the local cache.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: MovementEntity)

    /**
     * Inserts or replaces multiple movements in the local cache.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovements(movements: List<MovementEntity>)

    /**
     * Observes all movements for a specific user.
     * Returns a Flow that emits whenever the data changes.
     */
    @Query("SELECT * FROM movements WHERE userUid = :userUid ORDER BY datetime DESC")
    fun observeMovementsByUser(userUid: String): Flow<List<MovementEntity>>

    /**
     * Gets all movements for a user.
     */
    @Query("SELECT * FROM movements WHERE userUid = :userUid ORDER BY datetime DESC")
    suspend fun getMovementsByUser(userUid: String): List<MovementEntity>

    /**
     * Gets movements for a user within a date range.
     */
    @Query("SELECT * FROM movements WHERE userUid = :userUid AND datetime BETWEEN :startDate AND :endDate ORDER BY datetime DESC")
    suspend fun getMovementsByUserAndDateRange(
        userUid: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<MovementEntity>

    /**
     * Gets movements by type for a user.
     */
    @Query("SELECT * FROM movements WHERE userUid = :userUid AND type = :type ORDER BY datetime DESC")
    suspend fun getMovementsByUserAndType(
        userUid: String,
        type: MovementType
    ): List<MovementEntity>

    /**
     * Gets a specific movement by ID.
     */
    @Query("SELECT * FROM movements WHERE id = :movementId")
    suspend fun getMovementById(movementId: String): MovementEntity?

    /**
     * Deletes a movement from the local cache.
     */
    @Query("DELETE FROM movements WHERE id = :movementId")
    suspend fun deleteMovement(movementId: String)

    /**
     * Deletes all movements for a user.
     */
    @Query("DELETE FROM movements WHERE userUid = :userUid")
    suspend fun deleteAllMovementsByUser(userUid: String)

    /**
     * Deletes all movements from the local cache.
     */
    @Query("DELETE FROM movements")
    suspend fun deleteAllMovements()

    /**
     * Gets the count of movements for a user.
     */
    @Query("SELECT COUNT(*) FROM movements WHERE userUid = :userUid")
    suspend fun getMovementCountByUser(userUid: String): Int
}
