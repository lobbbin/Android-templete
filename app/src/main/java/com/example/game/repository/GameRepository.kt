package com.example.game.repository

import com.example.game.model.*
import com.example.game.persistence.*
import kotlinx.coroutines.flow.*

/**
 * Mediates between Room (persistence) and GameEngine (logic).
 * Exposes a single [Flow<GameState>] that the ViewModel collects.
 */
class GameRepository(
    private val playerDao: PlayerStateDao,
    private val plotDao: FarmPlotDao,
    private val inventoryDao: InventoryDao,
    private val animalDao: AnimalDao,
    private val npcDao: NPCRelationshipDao,
    private val engine: GameEngine = GameEngine(),
) {
    /** Aggregate all tables into one GameState flow. */
    val gameState: Flow<GameState> = combine(
        playerDao.get(),
        plotDao.getAll(),
        inventoryDao.getAll(),
        animalDao.getAll(),
        npcDao.getAll(),
    ) { player, plots, inventory, animals, npcs ->
        if (player == null) engine.newGame("Farmer", "Green Acres")
        else GameState(
            player = player,
            plots = plots,
            inventory = inventory,
            animals = animals,
            npcRelationships = npcs,
        )
    }

    // ── Persistence helpers ──────────────────────────────────

    suspend fun saveState(state: GameState) {
        playerDao.upsert(state.player)
        plotDao.upsertAll(state.plots)
        // Inventory: delete + re-insert to handle removals
        state.inventory.forEach { inventoryDao.upsert(it) }
        state.animals.forEach { animalDao.upsert(it) }
        state.npcRelationships.forEach { npcDao.upsert(it) }
    }

    suspend fun resetAndSave(state: GameState) {
        playerDao.deleteAll()
        plotDao.deleteAll()
        animalDao.deleteAll()
        npcDao.deleteAll()
        saveState(state)
    }

    // ── Engine actions (logic + persist) ─────────────────────

    suspend fun advanceDay(state: GameState): Pair<GameState, List<GameLogMessage>> {
        val (newState, logs) = engine.advanceDay(state)
        // Reset daily NPC counters
        val resetNpcs = newState.npcRelationships.map { it.copy(giftsToday = 0, talkedToday = false) }
        val finalState = newState.copy(npcRelationships = resetNpcs)
        saveState(finalState)
        return Pair(finalState, logs)
    }

    suspend fun tillPlot(state: GameState, plotId: Long): Result<GameState> {
        val result = engine.tillPlot(state, plotId)
        if (result.isSuccess) saveState(result.getOrThrow())
        return result
    }

    suspend fun plantCrop(state: GameState, plotId: Long, crop: CropType): Result<GameState> {
        val result = engine.plantCrop(state, plotId, crop)
        if (result.isSuccess) saveState(result.getOrThrow())
        return result
    }

    suspend fun waterPlot(state: GameState, plotId: Long): Result<GameState> {
        val result = engine.waterPlot(state, plotId)
        if (result.isSuccess) saveState(result.getOrThrow())
        return result
    }

    suspend fun waterAllPlots(state: GameState): Result<GameState> {
        val result = engine.waterAllPlots(state)
        if (result.isSuccess) saveState(result.getOrThrow())
        return result
    }

    suspend fun harvestPlot(state: GameState, plotId: Long): Result<GameState> {
        val result = engine.harvestPlot(state, plotId)
        if (result.isSuccess) saveState(result.getOrThrow())
        return result
    }

    suspend fun harvestAllReady(state: GameState): Result<GameState> {
        val result = engine.harvestAllReady(state)
        if (result.isSuccess) saveState(result.getOrThrow())
        return result
    }

    suspend fun feedAnimals(state: GameState): Result<GameState> {
        val result = engine.feedAnimals(state)
        if (result.isSuccess) saveState(result.getOrThrow())
        return result
    }

    suspend fun collectAnimalProduct(state: GameState, animalId: Long): Result<GameState> {
        val result = engine.collectAnimalProduct(state, animalId)
        if (result.isSuccess) saveState(result.getOrThrow())
        return result
    }

    suspend fun buyAnimal(state: GameState, type: AnimalType, name: String): Result<GameState> {
        val result = engine.buyAnimal(state, type, name)
        if (result.isSuccess) saveState(result.getOrThrow())
        return result
    }

    suspend fun sellItem(state: GameState, itemId: Long, quantity: Int = 1): Result<GameState> {
        val result = engine.sellItem(state, itemId, quantity)
        if (result.isSuccess) saveState(result.getOrThrow())
        return result
    }

    suspend fun forage(state: GameState): Result<GameState> {
        val result = engine.forage(state)
        if (result.isSuccess) saveState(result.getOrThrow())
        return result
    }

    suspend fun fish(state: GameState): Result<GameState> {
        val result = engine.fish(state)
        if (result.isSuccess) saveState(result.getOrThrow())
        return result
    }

    suspend fun talkToNpc(state: GameState, npcId: NPCId): Result<GameState> {
        val result = engine.talkToNpc(state, npcId)
        if (result.isSuccess) saveState(result.getOrThrow())
        return result
    }

    suspend fun giftNpc(state: GameState, npcId: NPCId, giftType: ItemType): Result<GameState> {
        val result = engine.giftNpc(state, npcId, giftType)
        if (result.isSuccess) saveState(result.getOrThrow())
        return result
    }

    suspend fun rest(state: GameState): Pair<GameState, List<GameLogMessage>> {
        val (newState, logs) = engine.rest(state)
        saveState(newState)
        return Pair(newState, logs)
    }

    suspend fun startNewGame(name: String, farmName: String): GameState {
        val state = engine.newGame(name, farmName)
        resetAndSave(state)
        return state
    }

    // Engine reference for direct access if needed
    fun getEngine(): GameEngine = engine
}
