package com.example.game.model

/** The four seasons — drives crop availability and weather patterns. */
enum class Season(val displayName: String, val emoji: String) {
    SPRING("Spring", "🌸"),
    SUMMER("Summer", "☀️"),
    AUTUMN("Autumn", "🍂"),
    WINTER("Winter", "❄️"),
    ;

    fun next(): Season = entries[(ordinal + 1) % entries.size]
}
