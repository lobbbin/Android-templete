package com.example.game.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Relationship status with a specific NPC. */
@Entity(tableName = "npc_relationships")
data class NPCRelationship(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val npcId: NPCId,
    val hearts: Int = 0,            // 0–10
    val giftsToday: Int = 0,        // max 1 gift/day
    val talkedToday: Boolean = false,
)
