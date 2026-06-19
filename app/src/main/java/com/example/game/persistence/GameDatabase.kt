package com.example.game.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.game.model.*

@Database(
    entities = [
        PlayerState::class,
        FarmPlot::class,
        InventoryItem::class,
        Animal::class,
        NPCRelationship::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class GameDatabase : RoomDatabase() {
    abstract fun playerStateDao(): PlayerStateDao
    abstract fun farmPlotDao(): FarmPlotDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun animalDao(): AnimalDao
    abstract fun npcRelationshipDao(): NPCRelationshipDao

    companion object {
        @Volatile private var INSTANCE: GameDatabase? = null

        fun get(context: Context): GameDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    GameDatabase::class.java,
                    "country_life_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
