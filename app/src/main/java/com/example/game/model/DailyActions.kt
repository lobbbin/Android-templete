package com.example.game.model

/** What the player can do each day. Tracked to limit per-day actions. */
data class DailyActions(
    val wateredPlots: Set<Long> = emptySet(),
    val harvestedPlots: Set<Long> = emptySet(),
    val talkedToNpc: Set<NPCId> = emptySet(),
    val giftedNpc: Set<NPCId> = emptySet(),
    val fishedToday: Boolean = false,
    val foragedToday: Boolean = false,
    val fedAnimals: Boolean = false,
    val animalsCollected: Set<Long> = emptySet(),
)
