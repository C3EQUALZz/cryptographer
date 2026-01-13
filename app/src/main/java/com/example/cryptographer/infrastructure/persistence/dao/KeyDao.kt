package com.example.cryptographer.infrastructure.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.cryptographer.infrastructure.persistence.models.KeyEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for encryption keys.
 *
 * Provides methods for CRUD operations on encryption keys stored in Room database.
 */
@Dao
interface KeyDao {
    /**
     * Inserts a new encryption key.
     *
     * @param keyEntity Key entity to insert
     * @return Row ID of inserted key
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKey(keyEntity: KeyEntity): Long

    /**
     * Updates an existing encryption key.
     *
     * @param keyEntity Key entity to update
     */
    @Update
    suspend fun updateKey(keyEntity: KeyEntity)

    /**
     * Deletes an encryption key by ID.
     *
     * @param keyId Key identifier
     */
    @Query("DELETE FROM encryption_keys WHERE id = :keyId")
    suspend fun deleteKeyById(keyId: String)

    /**
     * Deletes all encryption keys.
     */
    @Query("DELETE FROM encryption_keys")
    suspend fun deleteAllKeys()

    /**
     * Retrieves an encryption key by ID.
     *
     * @param keyId Key identifier
     * @return Key entity or null if not found
     */
    @Query("SELECT * FROM encryption_keys WHERE id = :keyId LIMIT 1")
    suspend fun getKeyById(keyId: String): KeyEntity?

    /**
     * Retrieves all encryption key IDs.
     *
     * @return List of key IDs
     */
    @Query("SELECT id FROM encryption_keys")
    suspend fun getAllKeyIds(): List<String>

    /**
     * Retrieves all encryption keys as a Flow.
     *
     * @return Flow of all key entities
     */
    @Query("SELECT * FROM encryption_keys")
    fun getAllKeysFlow(): Flow<List<KeyEntity>>

    /**
     * Retrieves all encryption keys.
     *
     * @return List of all key entities
     */
    @Query("SELECT * FROM encryption_keys")
    suspend fun getAllKeys(): List<KeyEntity>
}
