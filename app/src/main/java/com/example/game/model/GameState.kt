package com.example.game.model

/**
 * The full game state exposed to the UI — assembled from PresidentState + all related tables.
 * This is a UI-only aggregate; it is NOT an Entity.
 */
data class GameState(
    val president: PresidentState,
    val policies: List<Policy> = emptyList(),
    val nations: List<Nation> = emptyList(),
    val activeCrises: List<Crisis> = emptyList(),
    val cabinet: List<CabinetMember> = emptyList(),
    val logMessages: List<GameLogMessage> = emptyList(),
)

/** A member of the president's cabinet. */
data class CabinetMember(
    val id: String,
    val name: String,
    val role: String,
    val emoji: String,
    val competence: Int,        // 1-10
    val loyalty: Int,           // 1-10
    val scandal: Boolean = false,
)

/** A single message in the event log. */
data class GameLogMessage(
    val day: Int,
    val quarter: Quarter,
    val year: Int,
    val message: String,
    val emoji: String = "",
)