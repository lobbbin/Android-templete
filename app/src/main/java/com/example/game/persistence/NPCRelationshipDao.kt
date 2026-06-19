package com.example.game.persistence

import androidx.room.*
import com.example.game.model.NPCRelationship
import kotlinx.coroutines.flow.Flow

@Dao
interface NPCRelationshipDao {
    @Query("SELECT * FROM npc_relationships")
    fun getAll(): Flow<List<NPCRelationship>>

    @Query("SELECT * FROM npc_relationships WHERE npcId = :npcId")
    suspend fun getByNpc(npcId: String): NPCRelationship?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(rel: NPCRelationship): Long

    @Update
    suspend fun update(rel: NPCRelationship)

    @Query("DELETE FROM npc_relationships")
    suspend fun deleteAll()
}
