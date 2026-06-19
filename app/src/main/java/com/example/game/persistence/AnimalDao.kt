package com.example.game.persistence

import androidx.room.*
import com.example.game.model.Animal
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimalDao {
    @Query("SELECT * FROM animals")
    fun getAll(): Flow<List<Animal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(animal: Animal): Long

    @Update
    suspend fun update(animal: Animal)

    @Delete
    suspend fun delete(animal: Animal)

    @Query("DELETE FROM animals")
    suspend fun deleteAll()
}
