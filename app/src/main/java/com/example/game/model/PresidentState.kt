package com.example.game.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.game.persistence.Converters

/**
 * President and nation state — persisted as a single row in Room.
 */
@Entity(tableName = "president_state")
@TypeConverters(Converters::class)
data class PresidentState(
    @PrimaryKey val id: Long = 1,              // singleton row
    val presidentName: String = "Mr. President",
    val countryName: String = "Republic",
    val term: Int = 1,                         // current term (1-2 for 8-year max)
    val quarter: Quarter = Quarter.Q1,
    val year: Int = 1,
    val dayInQuarters: Int = 1,                // day within current quarter (1-90)
    
    // Core metrics
    val approvalRating: Int = 50,              // 0-100%
    val gdpGrowth: Int = 2,                    // annual %
    val treasury: Int = 10000,                 // millions $
    val nationalDebt: Int = 30000,             // millions $
    val unemployment: Int = 5,                 // %
    val inflation: Int = 2,                    // %
    
    // Political capital
    val politicalCapital: Int = 50,            // 0-100, spent on policies
    val congressSupport: Int = 50,             // 0-100, affects policy success
    val internationalReputation: Int = 50,     // 0-100
    
    // Stats
    val billsEnacted: Int = 0,
    val crisesResolved: Int = 0,
    val warsStarted: Int = 0,
    val alliancesFormed: Int = 0,
    val daysInOffice: Int = 0,
    
    // Flags
    val isAtWar: Boolean = false,
    val impeached: Boolean = false,
    val reelected: Boolean = false,
)