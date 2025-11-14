package com.grupo03.solea.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Room database for the Solea application.
 * Provides local caching for movements to improve performance and enable offline access.
 */
@Database(
    entities = [MovementEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SoleaDatabase : RoomDatabase() {

    /**
     * Provides access to Movement DAO.
     */
    abstract fun movementDao(): MovementDao

    companion object {
        const val DATABASE_NAME = "solea_database"
    }
}
