package com.example.game.model

/** All items the player can own — crops, tools, gifts, forage. */
enum class ItemType(val displayName: String, val emoji: String, val sellPrice: Int, val category: ItemCategory) {
    // Crops (harvested)
    TURNIP_BUNDLE("Turnip Bundle", "🥕", 60, ItemCategory.CROP),
    POTATO_BUNDLE("Potato Sack", "🥔", 80, ItemCategory.CROP),
    STRAWBERRY_BASKET("Strawberry Basket", "🍓", 130, ItemCategory.CROP),
    TOMATO_CRATE("Tomato Crate", "🍅", 100, ItemCategory.CROP),
    CORN_BUNDLE("Corn Bundle", "🌽", 150, ItemCategory.CROP),
    MELON("Melon", "🍈", 250, ItemCategory.CROP),
    PUMPKIN("Pumpkin", "🎃", 200, ItemCategory.CROP),
    EGGPLANT_BUNDLE("Eggplant Bundle", "🍆", 90, ItemCategory.CROP),
    CARROT_BUNDLE("Carrot Bundle", "🥕", 70, ItemCategory.CROP),
    CABBAGE_HEAD("Cabbage Head", "🥬", 85, ItemCategory.CROP),
    SNOW_PEA_POD("Snow Pea Pod", "🫛", 120, ItemCategory.CROP),

    // Forage
    WILD_HERB("Wild Herb", "🌿", 15, ItemCategory.FORAGE),
    MUSHROOM("Mushroom", "🍄", 25, ItemCategory.FORAGE),
    WILD_BERRY("Wild Berries", "🫐", 20, ItemCategory.FORAGE),
    HONEY("Honey Jar", "🍯", 50, ItemCategory.FORAGE),
    TRUFFLE("Truffle", "🟤", 100, ItemCategory.FORAGE),

    // Animal products
    EGG("Egg", "🥚", 30, ItemCategory.ANIMAL_PRODUCT),
    MILK("Milk Bottle", "🥛", 40, ItemCategory.ANIMAL_PRODUCT),
    WOOL("Wool Bundle", "🧶", 75, ItemCategory.ANIMAL_PRODUCT),

    // Tools
    WATERING_CAN("Watering Can", "🚿", 0, ItemCategory.TOOL),
    HOE("Hoe", "⛏️", 0, ItemCategory.TOOL),
    FISHING_ROD("Fishing Rod", "🎣", 0, ItemCategory.TOOL),

    // Gifts
    BOUQUET("Bouquet", "💐", 50, ItemCategory.GIFT),
    HOME_COOKED_MEAL("Home-Cooked Meal", "🍲", 30, ItemCategory.GIFT),
    GEMSTONE("Gemstone", "💎", 200, ItemCategory.GIFT),
    ;
}

enum class ItemCategory(val displayName: String) {
    CROP("Crops"),
    FORAGE("Forage"),
    ANIMAL_PRODUCT("Animal Products"),
    TOOL("Tools"),
    GIFT("Gifts"),
}
