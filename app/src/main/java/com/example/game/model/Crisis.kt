package com.example.game.model

/** A current crisis or event the president must respond to. */
data class Crisis(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val type: CrisisType,
    val severity: Int,  // 1-10
    val options: List<CrisisOption>,
    val resolved: Boolean = false,
)

enum class CrisisType(val displayName: String) {
    ECONOMIC("Economic"),
    MILITARY("Military"),
    DISASTER("Natural Disaster"),
    SCANDAL("Political Scandal"),
    DIPLOMATIC("Diplomatic"),
    HEALTH("Health Crisis"),
    ;
}

data class CrisisOption(
    val id: String,
    val text: String,
    val economicImpact: Int,
    val approvalImpact: Int,
    val budgetImpact: Int,
    val description: String,
)