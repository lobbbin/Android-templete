package com.example.game.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/** An owned animal instance. */
@Entity(tableName = "animals")
data class Animal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: AnimalType,
    val name: String,
    val happiness: Int = 50,       // 0–100
    val daysSinceProduct: Int = 0,
    val isFed: Boolean = false,
)
