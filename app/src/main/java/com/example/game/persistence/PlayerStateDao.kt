package com.example.game.persistence

import androidx.room.*
import com.example.game.model.PlayerState
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerStateDao {
    @Query("SELECT * FROM player_state WHERE id = 1")
    fun get(): Flow<PlayerState?>

    @Query("SELECT * FROM player_state WHERE id = 1")
    suspend fun getOnce(): PlayerState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: PlayerState)

    @Query("DELETE FROM player_state")
    suspend fun deleteAll()
}
