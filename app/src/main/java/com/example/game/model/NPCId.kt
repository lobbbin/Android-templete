package com.example.game.model

/** A villager the player can befriend, romance, or trade with. */
enum class NPCId(val displayName: String, val emoji: String, val role: String, val personality: String, val likedGifts: List<ItemType>) {
    ROSE("Rose", "👩‍🌾", "Farmhand", "Hardworking and kind. Loves fresh produce.", listOf(ItemType.STRAWBERRY_BASKET, ItemType.HONEY, ItemType.BOUQUET)),
    JASPER("Jasper", "👨‍🔬", "Botanist", "Quiet, studious. Appreciates rare plants.", listOf(ItemType.WILD_HERB, ItemType.TRUFFLE, ItemType.GEMSTONE)),
    PIP("Pip", "🧒", "Shopkeeper", "Cheerful and chatty. Fond of sweets.", listOf(ItemType.WILD_BERRY, ItemType.HOME_COOKED_MEAL, ItemType.MELON)),
    HAZEL("Hazel", "👩‍🍳", "Baker", "Warm and nurturing. Values home-cooking.", listOf(ItemType.EGG, ItemType.MILK, ItemType.HOME_COOKED_MEAL)),
    FINN("Finn", "🧓", "Fisherman", "Cranky but wise. Respects patience.", listOf(ItemType.MUSHROOM, ItemType.HOME_COOKED_MEAL)),
    LUNA("Luna", "👩‍🎨", "Artist", "Dreamy and creative. Loves beautiful things.", listOf(ItemType.BOUQUET, ItemType.GEMSTONE, ItemType.PUMPKIN)),
    ;
}
