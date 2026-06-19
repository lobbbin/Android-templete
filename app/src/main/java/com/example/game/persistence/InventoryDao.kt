package com.example.game.persistence

import androidx.room.*
import com.example.game.model.InventoryItem
import com.example.game.model.ItemType
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory")
    fun getAll(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory WHERE itemType = :itemType")
    suspend fun getByType(itemType: ItemType): InventoryItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: InventoryItem): Long

    @Update
    suspend fun update(item: InventoryItem)

    @Delete
    suspend fun delete(item: InventoryItem)

    @Query("DELETE FROM inventory WHERE itemType = :itemType")
    suspend fun deleteByType(itemType: ItemType)
}
