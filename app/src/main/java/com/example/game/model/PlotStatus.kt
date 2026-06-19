package com.example.game.model

/** Status of a single farm plot. */
enum class PlotStatus(val displayName: String, val emoji: String) {
    EMPTY("Empty", "🟫"),
    TILLED("Tilled", "🟫"),
    PLANTED("Planted", "🌱"),
    GROWING("Growing", "🌿"),
    READY("Ready to Harvest", "🌾"),
    WITHERED("Withered", "🥀"),
}
