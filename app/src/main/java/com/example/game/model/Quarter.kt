package com.example.game.model

/** The four quarters of a presidential term. */
enum class Quarter(val displayName: String, val emoji: String) {
    Q1("Q1 (Jan-Mar)", "📊"),
    Q2("Q2 (Apr-Jun)", "📈"),
    Q3("Q3 (Jul-Sep)", "🌡️"),
    Q4("Q4 (Oct-Dec)", "❄️"),
    ;

    fun next(): Quarter = entries[(ordinal + 1) % entries.size]
}