package com.example.game.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/** An inventory slot — stacks of a single ItemType. */
@Entity(tableName = "inventory")
data class InventoryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemType: ItemType,
    val quantity: Int = 1,
    val quality: Float = 1.0f,
)
