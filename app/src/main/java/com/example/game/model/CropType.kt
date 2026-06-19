package com.example.game.model

/** A crop that can be planted, grown, and harvested. */
enum class CropType(val displayName: String, val buyPrice: Int, val sellPrice: Int, val growDays: Int, val season: Season, val waterNeed: Int) {
    TURNIP("Turnip", 20, 60, 4, Season.SPRING, 1),
    POTATO("Potato", 30, 80, 6, Season.SPRING, 2),
    STRAWBERRY("Strawberry", 50, 130, 8, Season.SPRING, 2),
    TOMATO("Tomato", 40, 100, 6, Season.SUMMER, 2),
    CORN("Corn", 60, 150, 8, Season.SUMMER, 3),
    MELON("Melon", 80, 250, 12, Season.SUMMER, 3),
    PUMPKIN("Pumpkin", 70, 200, 10, Season.AUTUMN, 2),
    EGGPLANT("Eggplant", 35, 90, 5, Season.AUTUMN, 2),
    CARROT("Carrot", 25, 70, 5, Season.AUTUMN, 1),
    CABBAGE("Cabbage", 30, 85, 6, Season.WINTER, 1),
    SNOW_PEA("Snow Pea", 45, 120, 7, Season.WINTER, 2),
    ;

    companion object {
        fun availableIn(season: Season): List<CropType> = entries.filter { it.season == season }
    }
}
