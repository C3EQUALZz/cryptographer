package com.example.cryptographer.infrastructure.persistence.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.cryptographer.infrastructure.persistence.dao.KeyDao
import com.example.cryptographer.infrastructure.persistence.dao.SettingsDao
import com.example.cryptographer.infrastructure.persistence.models.KeyEntity
import com.example.cryptographer.infrastructure.persistence.models.SettingsEntity

/**
 * Room database for Cryptographer application.
 *
 * Stores encrypted encryption keys and application settings.
 * Uses Android Keystore for master key encryption.
 */
@Database(
    entities = [KeyEntity::class, SettingsEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class CryptographerDatabase : RoomDatabase() {
    /**
     * Data Access Object for encryption keys.
     */
    abstract fun keyDao(): KeyDao

    /**
     * Data Access Object for application settings.
     */
    abstract fun settingsDao(): SettingsDao

    companion object {
        private const val DATABASE_NAME = "cryptographer_database"

        /**
         * Creates and returns database instance.
         *
         * @param context Application context
         * @return Database instance
         */
        fun create(context: Context): CryptographerDatabase {
            return Room.databaseBuilder(
                context,
                CryptographerDatabase::class.java,
                DATABASE_NAME,
            )
                .fallbackToDestructiveMigration(false) // For development - remove in production
                .build()
        }
    }
}
