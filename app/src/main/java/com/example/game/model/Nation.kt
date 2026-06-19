package com.example.game.model

/** Nation's international relationships. */
enum class DiplomaticStatus(val displayName: String, val emoji: String) {
    ALLY("Ally", "🤝"),
    FRIENDLY("Friendly", "😊"),
    NEUTRAL("Neutral", "😐"),
    TENSE("Tense", "😠"),
    HOSTILE("Hostile", "😡"),
    AT_WAR("At War", "⚔️"),
    ;
}

/** A foreign nation for diplomacy mechanics. */
data class Nation(
    val id: String,
    val name: String,
    val emoji: String,
    val relationship: DiplomaticStatus = DiplomaticStatus.NEUTRAL,
    val tradeValue: Int = 0,      // millions in trade
    val militaryThreat: Int = 0,  // 0-100
    val aidGiven: Int = 0,        // foreign aid given (millions)
)