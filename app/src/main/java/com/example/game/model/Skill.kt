package com.example.game.model

/** Player skill levels — improve through use. */
enum class SkillType(val displayName: String, val emoji: String) {
    FARMING("Farming", "🌾"),
    FORAGING("Foraging", "🌿"),
    FISHING("Fishing", "🎣"),
    RANCHING("Ranching", "🐄"),
    COOKING("Cooking", "🍲"),
    SOCIAL("Social", "💬"),
    ;
}

data class Skill(
    val type: SkillType,
    val level: Int = 1,
    val xp: Int = 0,
) {
    /** XP needed for next level — scales exponentially. */
    val xpToNext: Int get() = 100 * level * level

    /** Add XP; returns [newLevel, leveledUp]. */
    fun addXp(amount: Int): Pair<Skill, Boolean> {
        var newLevel = level
        var newXp = xp + amount
        var leveledUp = false
        while (newXp >= 100 * newLevel * newLevel && newLevel < MAX_LEVEL) {
            newXp -= 100 * newLevel * newLevel
            newLevel++
            leveledUp = true
        }
        return Pair(copy(level = newLevel, xp = newXp), leveledUp)
    }

    companion object {
        const val MAX_LEVEL = 50
    }
}
