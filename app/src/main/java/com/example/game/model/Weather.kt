package com.example.game.model

/** Day's weather — affects crop watering, animal mood, and foraging. */
enum class Weather(val displayName: String, val emoji: String, val watersCrops: Boolean, val forageBonus: Float, val description: String) {
    SUNNY("Sunny", "☀️", false, 1.0f, "A bright, warm day. Water your crops!"),
    CLOUDY("Cloudy", "☁️", false, 1.1f, "Overcast skies. A chance of rain later."),
    RAINY("Rainy", "🌧️", true, 1.2f, "Rain waters all crops automatically."),
    STORMY("Stormy", "⛈️", true, 0.8f, "Heavy rain + wind. Crops are watered but may take damage."),
    SNOWY("Snowy", "🌨️", true, 0.5f, "Gentle snowfall. Everything moves slower."),
    ;

    companion object {
        /** Generate random weather biased by season. */
        fun forSeason(season: Season): Weather {
            val weights = when (season) {
                Season.SPRING -> mapOf(SUNNY to 3, CLOUDY to 3, RAINY to 3, STORMY to 1)
                Season.SUMMER -> mapOf(SUNNY to 5, CLOUDY to 2, RAINY to 2, STORMY to 1)
                Season.AUTUMN -> mapOf(SUNNY to 2, CLOUDY to 3, RAINY to 3, STORMY to 1, SNOWY to 1)
                Season.WINTER -> mapOf(SUNNY to 1, CLOUDY to 2, RAINY to 1, SNOWY to 5, STORMY to 1)
            }
            val total = weights.values.sum()
            var roll = (1..total).random()
            for ((w, wt) in weights) {
                roll -= wt
                if (roll <= 0) return w
            }
            return SUNNY
        }
    }
}
