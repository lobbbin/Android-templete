package com.example.game.model

/** An animal on the player's farm. */
enum class AnimalType(val displayName: String, val emoji: String, val buyPrice: Int, val product: ItemType, val productDays: Int) {
    CHICKEN("Chicken", "🐔", 500, ItemType.EGG, 1),
    COW("Cow", "🐄", 2000, ItemType.MILK, 1),
    SHEEP("Sheep", "🐑", 1500, ItemType.WOOL, 3),
    ;
}
