package com.example.game.engine

import com.example.game.model.*

/**
 * Pure-logic game engine. No Android dependencies — fully testable.
 * All state mutations return a new [GameState]; nothing is mutated in place.
 */
class GameEngine {

    // ── Day advancement ────────────────────────────────────

    /** Advance to the next day: grow crops, weather roll, season/year check. */
    fun advanceDay(state: GameState): Pair<GameState, List<GameLogMessage>> {
        val logs = mutableListOf<GameLogMessage>()
        var player = state.player

        // 1. Determine next day / season / year
        val newDay = player.day + 1
        val daysPerSeason = 28
        val (newSeason, newYear) = if (newDay > daysPerSeason) {
            val nextSeason = player.season.next()
            val yearBump = if (nextSeason == Season.SPRING) player.year + 1 else player.year
            Pair(nextSeason, yearBump)
        } else {
            Pair(player.season, player.year)
        }
        val resetDay = if (newDay > daysPerSeason) 1 else newDay

        // 2. New weather
        val newWeather = Weather.forSeason(newSeason)
        logs.add(GameLogMessage(resetDay, newSeason, newYear,
            "Day $resetDay of ${newSeason.displayName}, Year $newYear — ${newWeather.displayName}",
            newWeather.emoji))

        // 3. Grow crops & apply weather
        val updatedPlots = state.plots.map { plot ->
            when (plot.status) {
                PlotStatus.PLANTED, PlotStatus.GROWING -> {
                    val watered = plot.isWatered || newWeather.watersCrops
                    val stormDamage = newWeather == Weather.STORMY && (1..10).random() <= 2
                    if (stormDamage) {
                        logs.add(GameLogMessage(resetDay, newSeason, newYear,
                            "A storm damaged your ${plot.crop?.displayName ?: "crop"}!", "⛈️"))
                        plot.copy(status = PlotStatus.WITHERED, isWatered = false)
                    } else {
                        val newDays = if (watered) plot.daysPlanted + 1 else plot.daysPlanted
                        val crop = plot.crop
                        val isReady = crop != null && newDays >= crop.growDays
                        if (isReady) {
                            logs.add(GameLogMessage(resetDay, newSeason, newYear,
                                "${crop.displayName} is ready to harvest!", "🌾"))
                            plot.copy(status = PlotStatus.READY, daysPlanted = newDays, isWatered = false)
                        } else {
                            plot.copy(status = PlotStatus.GROWING, daysPlanted = newDays, isWatered = false)
                        }
                    }
                }
                else -> plot.copy(isWatered = false)
            }
        }

        // 4. Animal production
        val updatedAnimals = state.animals.map { animal ->
            val daysSince = if (animal.isFed) animal.daysSinceProduct + 1 else 0
            val productReady = daysSince >= animal.type.productDays
            if (productReady && animal.isFed) {
                logs.add(GameLogMessage(resetDay, newSeason, newYear,
                    "${animal.name} the ${animal.type.displayName} has ${animal.type.product.displayName}!", animal.type.emoji))
            }
            animal.copy(daysSinceProduct = daysSince, isFed = false, happiness = (animal.happiness - 2).coerceIn(0, 100))
        }

        // 5. Reset player energy
        player = player.copy(
            day = resetDay,
            season = newSeason,
            year = newYear,
            weather = newWeather,
            energy = player.maxEnergy,
            daysPlayed = player.daysPlayed + 1,
        )

        return Pair(
            state.copy(player = player, plots = updatedPlots, animals = updatedAnimals, logMessages = logs + state.logMessages),
            logs
        )
    }

    // ── Farm actions ───────────────────────────────────────

    fun tillPlot(state: GameState, plotId: Long, energyCost: Int = 5): Result<GameState> {
        val plot = state.plots.find { it.id == plotId } ?: return Result.failure(IllegalArgumentException("Plot not found"))
        if (plot.status != PlotStatus.EMPTY) return Result.failure(IllegalStateException("Plot is not empty"))
        val player = deductEnergy(state.player, energyCost) ?: return Result.failure(IllegalStateException("Not enough energy"))
        val updated = plot.copy(status = PlotStatus.TILLED)
        return Result.success(state.copy(player = player, plots = state.plots.map { if (it.id == plotId) updated else it }))
    }

    fun plantCrop(state: GameState, plotId: Long, crop: CropType, energyCost: Int = 5): Result<GameState> {
        val plot = state.plots.find { it.id == plotId } ?: return Result.failure(IllegalArgumentException("Plot not found"))
        if (plot.status != PlotStatus.TILLED) return Result.failure(IllegalStateException("Plot must be tilled first"))
        if (crop.season != state.player.season && state.player.season != Season.WINTER) {
            // Allow winter crops in winter; block mismatched seasons
            if (crop.season != state.player.season) return Result.failure(IllegalStateException("${crop.displayName} can only be planted in ${crop.season.displayName}"))
        }
        val player = deductEnergy(state.player, energyCost) ?: return Result.failure(IllegalStateException("Not enough energy"))
        if (player.gold < crop.buyPrice) return Result.failure(IllegalStateException("Not enough gold (need ${crop.buyPrice}g)"))

        val updatedPlot = plot.copy(status = PlotStatus.PLANTED, crop = crop, daysPlanted = 0, isWatered = false)
        val updatedPlayer = player.copy(gold = player.gold - crop.buyPrice)
        return Result.success(state.copy(player = updatedPlayer, plots = state.plots.map { if (it.id == plotId) updatedPlot else it }))
    }

    fun waterPlot(state: GameState, plotId: Long, energyCost: Int = 3): Result<GameState> {
        val plot = state.plots.find { it.id == plotId } ?: return Result.failure(IllegalArgumentException("Plot not found"))
        if (plot.status !in listOf(PlotStatus.PLANTED, PlotStatus.GROWING)) return Result.failure(IllegalStateException("Nothing to water here"))
        if (plot.isWatered) return Result.failure(IllegalStateException("Already watered today"))
        val player = deductEnergy(state.player, energyCost) ?: return Result.failure(IllegalStateException("Not enough energy"))
        val updated = plot.copy(isWatered = true)
        return Result.success(state.copy(player = player, plots = state.plots.map { if (it.id == plotId) updated else it }))
    }

    fun waterAllPlots(state: GameState, energyCost: Int = 3): Result<GameState> {
        val waterable = state.plots.filter { it.status in listOf(PlotStatus.PLANTED, PlotStatus.GROWING) && !it.isWatered }
        if (waterable.isEmpty()) return Result.success(state) // nothing to water
        val totalCost = waterable.size * energyCost
        val player = deductEnergy(state.player, totalCost) ?: return Result.failure(IllegalStateException("Not enough energy (need $totalCost)"))
        val updatedPlots = state.plots.map { p ->
            if (p.id in waterable.map { it.id }) p.copy(isWatered = true) else p
        }
        return Result.success(state.copy(player = player, plots = updatedPlots))
    }

    fun harvestPlot(state: GameState, plotId: Long, energyCost: Int = 3): Result<GameState> {
        val plot = state.plots.find { it.id == plotId } ?: return Result.failure(IllegalArgumentException("Plot not found"))
        if (plot.status != PlotStatus.READY) return Result.failure(IllegalStateException("Crop not ready"))
        val crop = plot.crop ?: return Result.failure(IllegalStateException("No crop"))
        val player = deductEnergy(state.player, energyCost) ?: return Result.failure(IllegalStateException("Not enough energy"))

        // Map crop to harvest item
        val harvestItem = cropToItem(crop)
        val quality = (0.8f..1.5f).random().coerceIn(0.5f, 2.0f)
        val existing = state.inventory.find { it.itemType == harvestItem }
        val newInventory = if (existing != null) {
            state.inventory.map { if (it.itemType == harvestItem) it.copy(quantity = it.quantity + 1, quality = maxOf(it.quality, quality)) else it }
        } else {
            state.inventory + InventoryItem(itemType = harvestItem, quantity = 1, quality = quality)
        }

        val yieldGold = (crop.sellPrice * quality).toInt()
        val xpGain = crop.growDays * 5

        val updatedPlot = plot.copy(status = PlotStatus.TILLED, crop = null, daysPlanted = 0, isWatered = false)
        val updatedPlayer = player.copy(
            totalHarvests = player.totalHarvests + 1,
            farmingXp = player.farmingXp + xpGain,
        )
        // Check farming level-up
        val (newFarmingLevel, newFarmingXp, farmingLevelUp) = checkLevelUp(updatedPlayer.farmingLevel, updatedPlayer.farmingXp)

        val finalPlayer = updatedPlayer.copy(farmingLevel = newFarmingLevel, farmingXp = newFarmingXp)
        val levelUpLog = if (farmingLevelUp) GameLogMessage(player.day, player.season, player.year, "Farming level up! Now level $newFarmingLevel!", "⬆️") else null

        val newLogs = mutableListOf<GameLogMessage>()
        newLogs.add(GameLogMessage(player.day, player.season, player.year,
            "Harvested ${crop.displayName} (quality ★${"%.1f".format(quality)}) — valued at ${yieldGold}g", "🌾"))
        if (levelUpLog != null) newLogs.add(levelUpLog)

        return Result.success(state.copy(
            player = finalPlayer,
            plots = state.plots.map { if (it.id == plotId) updatedPlot else it },
            inventory = newInventory,
            logMessages = newLogs + state.logMessages,
        ))
    }

    fun harvestAllReady(state: GameState, energyCost: Int = 3): Result<GameState> {
        val readyPlots = state.plots.filter { it.status == PlotStatus.READY }
        if (readyPlots.isEmpty()) return Result.success(state)
        var current = state
        for (plot in readyPlots) {
            val result = harvestPlot(current, plot.id, energyCost)
            if (result.isFailure) break
            current = result.getOrThrow()
        }
        return Result.success(current)
    }

    // ── Animal actions ──────────────────────────────────────

    fun feedAnimals(state: GameState): Result<GameState> {
        val cost = state.animals.size * 3 // 3g per animal feed
        if (state.player.gold < cost) return Result.failure(IllegalStateException("Need ${cost}g for feed"))
        val player = deductEnergy(state.player, state.animals.size * 2) ?: return Result.failure(IllegalStateException("Not enough energy"))
        val updatedAnimals = state.animals.map { it.copy(isFed = true, happiness = (it.happiness + 5).coerceIn(0, 100)) }
        return Result.success(state.copy(player = player.copy(gold = player.gold - cost), animals = updatedAnimals))
    }

    fun collectAnimalProduct(state: GameState, animalId: Long): Result<GameState> {
        val animal = state.animals.find { it.id == animalId } ?: return Result.failure(IllegalArgumentException("Animal not found"))
        if (!animal.isFed) return Result.failure(IllegalStateException("${animal.name} needs to be fed first"))
        if (animal.daysSinceProduct < animal.type.productDays) return Result.failure(IllegalStateException("${animal.name} hasn't produced yet"))
        val player = deductEnergy(state.player, 2) ?: return Result.failure(IllegalStateException("Not enough energy"))

        val product = animal.type.product
        val existing = state.inventory.find { it.itemType == product }
        val newInventory = if (existing != null) {
            state.inventory.map { if (it.itemType == product) it.copy(quantity = it.quantity + 1) else it }
        } else {
            state.inventory + InventoryItem(itemType = product, quantity = 1)
        }

        val updatedAnimal = animal.copy(daysSinceProduct = 0)
        val logs = listOf(GameLogMessage(player.day, player.season, player.year,
            "Collected ${product.displayName} from ${animal.name}!", product.emoji))

        return Result.success(state.copy(
            player = player.copy(ranchingXp = player.ranchingXp + 10),
            animals = state.animals.map { if (it.id == animalId) updatedAnimal else it },
            inventory = newInventory,
            logMessages = logs + state.logMessages,
        ))
    }

    fun buyAnimal(state: GameState, type: AnimalType, name: String): Result<GameState> {
        if (state.player.gold < type.buyPrice) return Result.failure(IllegalStateException("Need ${type.buyPrice}g"))
        val player = deductEnergy(state.player, 5) ?: return Result.failure(IllegalStateException("Not enough energy"))
        val newAnimal = Animal(type = type, name = name, happiness = 70)
        return Result.success(state.copy(
            player = player.copy(gold = player.gold - type.buyPrice),
            animals = state.animals + newAnimal,
        ))
    }

    // ── Shop / selling ─────────────────────────────────────

    fun sellItem(state: GameState, itemId: Long, quantity: Int = 1): Result<GameState> {
        val item = state.inventory.find { it.id == itemId } ?: return Result.failure(IllegalArgumentException("Item not found"))
        if (item.quantity < quantity) return Result.failure(IllegalStateException("Only have ${item.quantity}"))
        val sellValue = (item.itemType.sellPrice * item.quality * quantity).toInt()
        val sellable = item.itemType.sellPrice > 0
        if (!sellable) return Result.failure(IllegalStateException("Can't sell ${item.itemType.displayName}"))

        val newInventory = if (item.quantity == quantity) {
            state.inventory.filter { it.id != itemId }
        } else {
            state.inventory.map { if (it.id == itemId) it.copy(quantity = it.quantity - quantity) else it }
        }

        val logs = listOf(GameLogMessage(state.player.day, state.player.season, state.player.year,
            "Sold ${quantity}× ${item.itemType.displayName} for ${sellValue}g", "💰"))

        return Result.success(state.copy(
            player = state.player.copy(gold = state.player.gold + sellValue, totalEarnings = state.player.totalEarnings + sellValue),
            inventory = newInventory,
            logMessages = logs + state.logMessages,
        ))
    }

    // ── Foraging ────────────────────────────────────────────

    fun forage(state: GameState): Result<GameState> {
        val player = deductEnergy(state.player, 8) ?: return Result.failure(IllegalStateException("Not enough energy"))
        val forageItems = listOf(ItemType.WILD_HERB, ItemType.MUSHROOM, ItemType.WILD_BERRY)
        val found = forageItems.random()
        val bonusMult = state.player.weather.forageBonus
        val quality = (0.5f..1.5f).random() * bonusMult

        val existing = state.inventory.find { it.itemType == found }
        val newInventory = if (existing != null) {
            state.inventory.map { if (it.itemType == found) it.copy(quantity = it.quantity + 1) else it }
        } else {
            state.inventory + InventoryItem(itemType = found, quantity = 1, quality = quality.coerceIn(0.5f, 2.0f))
        }

        val xpGain = 8
        val (newLevel, newXp, leveled) = checkLevelUp(player.foragingLevel, player.foragingXp + xpGain)
        val logs = mutableListOf(GameLogMessage(player.day, player.season, player.year,
            "Found ${found.displayName} while foraging!", found.emoji))
        if (leveled) logs.add(GameLogMessage(player.day, player.season, player.year, "Foraging level up! Now level $newLevel!", "⬆️"))

        return Result.success(state.copy(
            player = player.copy(foragingLevel = newLevel, foragingXp = newXp),
            inventory = newInventory,
            logMessages = logs + state.logMessages,
        ))
    }

    // ── Fishing ────────────────────────────────────────────

    fun fish(state: GameState): Result<GameState> {
        val player = deductEnergy(state.player, 10) ?: return Result.failure(IllegalStateException("Not enough energy"))
        val caught = (1..100).random()
        val skillBonus = player.fishingLevel / 5
        val success = caught + skillBonus > 40

        val logs = mutableListOf<GameLogMessage>()
        val (newLevel, newXp) = if (success) {
            val xp = 15
            val (l, x, lvUp) = checkLevelUp(player.fishingLevel, player.fishingXp + xp)
            if (lvUp) logs.add(GameLogMessage(player.day, player.season, player.year, "Fishing level up! Now level $l!", "⬆️"))
            Pair(l, x)
        } else {
            logs.add(GameLogMessage(player.day, player.season, player.year, "The fish got away...", "🎣"))
            val (l, x, _) = checkLevelUp(player.fishingLevel, player.fishingXp + 3)
            Pair(l, x)
        }

        if (success) {
            val fishType = if ((1..10).random() <= 3) ItemType.TRUFFLE else ItemType.WILD_HERB
            val existing = state.inventory.find { it.itemType == fishType }
            val newInventory = if (existing != null) {
                state.inventory.map { if (it.itemType == fishType) it.copy(quantity = it.quantity + 1) else it }
            } else {
                state.inventory + InventoryItem(itemType = fishType, quantity = 1)
            }
            logs.add(0, GameLogMessage(player.day, player.season, player.year, "Caught something! ${fishType.displayName}!", "🐟"))
            return Result.success(state.copy(player = player.copy(fishingLevel = newLevel, fishingXp = newXp), inventory = newInventory, logMessages = logs + state.logMessages))
        }

        return Result.success(state.copy(player = player.copy(fishingLevel = newLevel, fishingXp = newXp), logMessages = logs + state.logMessages))
    }

    // ── NPC interactions ────────────────────────────────────

    fun talkToNpc(state: GameState, npcId: NPCId): Result<GameState> {
        val player = deductEnergy(state.player, 2) ?: return Result.failure(IllegalStateException("Not enough energy"))
        var rel = state.npcRelationships.find { it.npcId == npcId } ?: NPCRelationship(npcId = npcId)
        if (rel.talkedToday) return Result.failure(IllegalStateException("Already talked to ${npcId.displayName} today"))

        rel = rel.copy(talkedToday = true)
        val (newSocialLevel, newSocialXp, leveled) = checkLevelUp(player.socialLevel, player.socialXp + 5)

        val dialogue = getNpcDialogue(npcId, rel.hearts)
        val logs = listOf(GameLogMessage(player.day, player.season, player.year,
            "${npcId.emoji} ${npcId.displayName}: \"$dialogue\"", npcId.emoji))

        val updatedRels = if (state.npcRelationships.any { it.npcId == npcId }) {
            state.npcRelationships.map { if (it.npcId == npcId) rel else it }
        } else {
            state.npcRelationships + rel
        }

        return Result.success(state.copy(
            player = player.copy(socialLevel = newSocialLevel, socialXp = newSocialXp),
            npcRelationships = updatedRels,
            logMessages = logs + state.logMessages,
        ))
    }

    fun giftNpc(state: GameState, npcId: NPCId, giftType: ItemType): Result<GameState> {
        val giftItem = state.inventory.find { it.itemType == giftType } ?: return Result.failure(IllegalStateException("Don't have that item"))
        var rel = state.npcRelationships.find { it.npcId == npcId } ?: NPCRelationship(npcId = npcId)
        if (rel.giftsToday >= 1) return Result.failure(IllegalStateException("Already gave a gift to ${npcId.displayName} today"))

        val isLiked = giftType in npcId.likedGifts
        val heartsGain = if (isLiked) 2 else 1
        rel = rel.copy(hearts = (rel.hearts + heartsGain).coerceIn(0, 10), giftsToday = rel.giftsToday + 1)

        // Remove one from inventory
        val newInventory = if (giftItem.quantity == 1) {
            state.inventory.filter { it.id != giftItem.id }
        } else {
            state.inventory.map { if (it.id == giftItem.id) it.copy(quantity = it.quantity - 1) else it }
        }

        val response = if (isLiked) "${npcId.displayName} loved the ${giftType.displayName}! (+$heartsGain ❤️)" else "${npcId.displayName} accepted the ${giftType.displayName}. (+$heartsGain ❤️)"
        val logs = listOf(GameLogMessage(state.player.day, state.player.season, state.player.year, response, npcId.emoji))

        val (newSocialLevel, newSocialXp, _) = checkLevelUp(state.player.socialLevel, state.player.socialXp + (if (isLiked) 15 else 5))
        val updatedRels = state.npcRelationships.map { if (it.npcId == npcId) rel else it }

        return Result.success(state.copy(
            player = state.player.copy(socialLevel = newSocialLevel, socialXp = newSocialXp),
            inventory = newInventory,
            npcRelationships = updatedRels,
            logMessages = logs + state.logMessages,
        ))
    }

    // ── Energy / rest ───────────────────────────────────────

    fun rest(state: GameState): Pair<GameState, List<GameLogMessage>> {
        val player = state.player.copy(energy = (state.player.energy + 30).coerceIn(0, state.player.maxEnergy))
        val logs = listOf(GameLogMessage(player.day, player.season, player.year, "Took a rest. Energy +30.", "😴"))
        return Pair(state.copy(player = player, logMessages = logs + state.logMessages), logs)
    }

    // ── New game ───────────────────────────────────────────

    fun newGame(name: String, farmName: String): GameState {
        val player = PlayerState(playerName = name, farmName = farmName, weather = Weather.SUNNY)
        val plots = (0..2).flatMap { y -> (0..2).map { x -> FarmPlot(gridX = x, gridY = y) } }
        val npcs = NPCId.entries.map { NPCRelationship(npcId = it) }
        return GameState(player = player, plots = plots, npcRelationships = npcs)
    }

    // ── Helpers ─────────────────────────────────────────────

    private fun deductEnergy(player: PlayerState, cost: Int): PlayerState? {
        val newEnergy = player.energy - cost
        return if (newEnergy < 0) null else player.copy(energy = newEnergy)
    }

    private fun checkLevelUp(level: Int, xp: Int): Triple<Int, Int, Boolean> {
        var l = level
        var x = xp
        var leveled = false
        while (x >= 100 * l * l && l < MAX_SKILL_LEVEL) {
            x -= 100 * l * l
            l++
            leveled = true
        }
        return Triple(l, x, leveled)
    }

    companion object {
        const val MAX_SKILL_LEVEL = 50
    }

    private fun cropToItem(crop: CropType): ItemType = when (crop) {
        CropType.TURNIP -> ItemType.TURNIP_BUNDLE
        CropType.POTATO -> ItemType.POTATO_SACK
        CropType.STRAWBERRY -> ItemType.STRAWBERRY_BASKET
        CropType.TOMATO -> ItemType.TOMATO_CRATE
        CropType.CORN -> ItemType.CORN_BUNDLE
        CropType.MELON -> ItemType.MELON
        CropType.PUMPKIN -> ItemType.PUMPKIN
        CropType.EGGPLANT -> ItemType.EGGPLANT_BUNDLE
        CropType.CARROT -> ItemType.CARROT_BUNDLE
        CropType.CABBAGE -> ItemType.CABBAGE_HEAD
        CropType.SNOW_PEA -> ItemType.SNOW_PEA_POD
    }

    private fun getNpcDialogue(npc: NPCId, hearts: Int): String {
        val base = when (npc) {
            NPCId.ROSE -> listOf("The fields need tending!", "Sun's up, time's wastin'.", "Good to see you, neighbor!", "You're becoming quite the farmer!", "I'm proud of you, you know that?")
            NPCId.JASPER -> listOf("Hmm? I'm cataloging specimens.", "Interesting soil composition today.", "Ah, back again?", "Your botanical knowledge impresses me.", "I consider you a true colleague.")
            NPCId.PIP -> listOf("Welcome! Want to buy something?", "New stock just arrived!", "You're my favorite customer!", "Tell me about your farm!", "You're like family now!")
            NPCId.HAZEL -> listOf("I just pulled bread from the oven.", "Care for a warm meal?", "Your farm produce is wonderful!", "Let me bake you something special.", "You've brought so much warmth to this town.")
            NPCId.FINN -> listOf("...", "Quiet day on the water.", "You're persistent, I'll give you that.", "Hmph. You're alright.", "You've earned my respect, kid.")
            NPCId.LUNA -> listOf("I'm painting the sunset...", "The light is beautiful today.", "Your farm is so inspiring!", "I'd love to paint your fields.", "You're my muse, you know?")
        }
        val idx = (hearts / 2).coerceIn(0, base.size - 1)
        return base[idx]
    }
}
