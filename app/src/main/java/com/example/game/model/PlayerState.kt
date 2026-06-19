package com.example.game.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.game.persistence.Converters

/**
 * Core player state — persisted as a single row in Room.
 * Carries all scalar fields; collections (plots, animals, inventory, NPCs)
 * live in their own tables and are joined at the repository layer.
 */
@Entity(tableName = "player_state")
@TypeConverters(Converters::class)
data class PlayerState(
    @PrimaryKey val id: Long = 1,          // singleton row
    val playerName: String = "Farmer",
    val farmName: String = "Green Acres",
    val gold: Int = 500,
    val day: Int = 1,
    val season: Season = Season.SPRING,
    val year: Int = 1,
    val weather: Weather = Weather.SUNNY,
    val energy: Int = 100,
    val maxEnergy: Int = 100,
    val farmingLevel: Int = 1,
    val farmingXp: Int = 0,
    val foragingLevel: Int = 1,
    val foragingXp: Int = 0,
    val fishingLevel: Int = 1,
    val fishingXp: Int = 0,
    val ranchingLevel: Int = 1,
    val ranchingXp: Int = 0,
    val cookingLevel: Int = 1,
    val cookingXp: Int = 0,
    val socialLevel: Int = 1,
    val socialXp: Int = 0,
    val totalEarnings: Long = 0,
    val totalHarvests: Int = 0,
    val daysPlayed: Int = 0,
    val hasPet: Boolean = false,
    val petName: String = "",
)
