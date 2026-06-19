package com.example.game.persistence

import androidx.room.*
import com.example.game.model.FarmPlot
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmPlotDao {
    @Query("SELECT * FROM farm_plots ORDER BY gridY, gridX")
    fun getAll(): Flow<List<FarmPlot>>

    @Query("SELECT * FROM farm_plots WHERE id = :id")
    suspend fun getById(id: Long): FarmPlot?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(plot: FarmPlot): Long

    @Update
    suspend fun update(plot: FarmPlot)

    @Delete
    suspend fun delete(plot: FarmPlot)

    @Query("DELETE FROM farm_plots")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(plots: List<FarmPlot>)
}
