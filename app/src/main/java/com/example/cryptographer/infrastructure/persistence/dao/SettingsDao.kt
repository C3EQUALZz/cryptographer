package com.example.cryptographer.infrastructure.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.cryptographer.infrastructure.persistence.models.SettingsEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for application settings.
 *
 * Provides methods for CRUD operations on settings stored in Room database.
 */
@Dao
interface SettingsDao {
    /**
     * Inserts or updates a setting.
     *
     * @param settingEntity Setting entity to insert/update
     * @return Row ID of inserted/updated setting
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSetting(settingEntity: SettingsEntity): Long

    /**
     * Updates an existing setting.
     *
     * @param settingEntity Setting entity to update
     */
    @Update
    suspend fun updateSetting(settingEntity: SettingsEntity)

    /**
     * Retrieves a setting by key.
     *
     * @param key Setting key
     * @return Setting entity or null if not found
     */
    @Query("SELECT * FROM settings WHERE `key` = :key LIMIT 1")
    suspend fun getSettingByKey(key: String): SettingsEntity?

    /**
     * Retrieves all settings as a Flow.
     *
     * @return Flow of all setting entities
     */
    @Query("SELECT * FROM settings")
    fun getAllSettingsFlow(): Flow<List<SettingsEntity>>

    /**
     * Retrieves all settings.
     *
     * @return List of all setting entities
     */
    @Query("SELECT * FROM settings")
    suspend fun getAllSettings(): List<SettingsEntity>
}
