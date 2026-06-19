package com.example.game.model

/** Government policy areas the president can enact. */
enum class PolicyCategory(val displayName: String, val emoji: String) {
    ECONOMY("Economy", "💰"),
    HEALTHCARE("Healthcare", "🏥"),
    EDUCATION("Education", "📚"),
    DEFENSE("Defense", "🛡️"),
    ENVIRONMENT("Environment", "🌍"),
    INFRASTRUCTURE("Infrastructure", "🏗️"),
    IMMIGRATION("Immigration", "🛂"),
    FOREIGN_AFFAIRS("Foreign Affairs", "🌐"),
    ;
}

/** A policy the president can enact with trade-offs. */
data class Policy(
    val id: String,
    val name: String,
    val description: String,
    val category: PolicyCategory,
    val economicImpact: Int,      // GDP change %
    val approvalImpact: Int,      // approval rating change
    val budgetImpact: Int,        // treasury change (millions)
    val freedomImpact: Int,       // civil liberties -10 to +10
    val enacted: Boolean = false,
)