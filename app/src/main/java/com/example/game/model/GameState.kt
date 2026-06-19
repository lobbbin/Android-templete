package com.example.game.model

/**
 * The full game state exposed to the UI — assembled from PlayerState + all related tables.
 * This is a UI-only aggregate; it is NOT an Entity.
 */
data class GameState(
    val player: PlayerState,
    val plots: List<FarmPlot> = emptyList(),
    val inventory: List<InventoryItem> = emptyList(),
    val animals: List<Animal> = emptyList(),
    val npcRelationships: List<NPCRelationship> = emptyList(),
    val logMessages: List<GameLogMessage> = emptyList(),
)

/** A single message in the event log — displayed in the UI as a text feed. */
data class GameLogMessage(
    val day: Int,
    val season: Season,
    val year: Int,
    val message: String,
    val emoji: String = "",
)
