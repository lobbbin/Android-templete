package com.example.game.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.game.engine.GameEngine
import com.example.game.model.*
import com.example.game.persistence.GameDatabase
import com.example.game.repository.GameRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Single ViewModel for the entire game.
 * Holds [GameState] as a MutableStateFlow that the UI collects.
 * All user actions funnel through here → Repository → Engine → persist.
 */
class GameViewModel(app: Application) : AndroidViewModel(app) {

    private val db = GameDatabase.get(app)
    private val repo = GameRepository(
        playerDao = db.playerStateDao(),
        plotDao = db.farmPlotDao(),
        inventoryDao = db.inventoryDao(),
        animalDao = db.animalDao(),
        npcDao = db.npcRelationshipDao(),
    )

    // ── UiState ─────────────────────────────────────────────

    private val _state = MutableStateFlow<GameState?>(null)
    val state: StateFlow<GameState?> = _state.asStateFlow()

    private val _snackbar = MutableStateFlow<String?>(null)
    val snackbar: StateFlow<String?> = _snackbar.asStateFlow()

    private val _dialogue = MutableStateFlow<NpcDialogue?>(null)
    val dialogue: StateFlow<NpcDialogue?> = _dialogue.asStateFlow()

    init {
        viewModelScope.launch {
            repo.gameState.collect { _state.value = it }
        }
    }

    // ── Actions ──────────────────────────────────────────────

    fun startNewGame(name: String, farmName: String) {
        viewModelScope.launch {
            val state = repo.startNewGame(name, farmName)
            _state.value = state
        }
    }

    fun advanceDay() = withCurrentState { state ->
        val (newState, logs) = repo.advanceDay(state)
        _state.value = newState
        logs.firstOrNull()?.let { _snackbar.value = it.message }
    }

    fun tillPlot(plotId: Long) = withCurrentState { state ->
        val result = repo.tillPlot(state, plotId)
        handleResult(result) { _snackbar.value = "Tilled the land! 🟫" }
    }

    fun plantCrop(plotId: Long, crop: CropType) = withCurrentState { state ->
        val result = repo.plantCrop(state, plotId, crop)
        handleResult(result) { _snackbar.value = "Planted ${crop.displayName}! 🌱" }
    }

    fun waterPlot(plotId: Long) = withCurrentState { state ->
        val result = repo.waterPlot(state, plotId)
        handleResult(result) { _snackbar.value = "Watered! 💧" }
    }

    fun waterAllPlots() = withCurrentState { state ->
        val result = repo.waterAllPlots(state)
        handleResult(result) { _snackbar.value = "Watered all plots! 💧" }
    }

    fun harvestPlot(plotId: Long) = withCurrentState { state ->
        val result = repo.harvestPlot(state, plotId)
        handleResult(result) {}
    }

    fun harvestAllReady() = withCurrentState { state ->
        val result = repo.harvestAllReady(state)
        handleResult(result) { _snackbar.value = "Harvested all ready crops! 🌾" }
    }

    fun feedAnimals() = withCurrentState { state ->
        val result = repo.feedAnimals(state)
        handleResult(result) { _snackbar.value = "Animals fed! 🐔" }
    }

    fun collectProduct(animalId: Long) = withCurrentState { state ->
        val result = repo.collectAnimalProduct(state, animalId)
        handleResult(result) {}
    }

    fun buyAnimal(type: AnimalType, name: String) = withCurrentState { state ->
        val result = repo.buyAnimal(state, type, name)
        handleResult(result) { _snackbar.value = "Bought ${type.displayName} — $name! ${type.emoji}" }
    }

    fun sellItem(itemId: Long, quantity: Int = 1) = withCurrentState { state ->
        val result = repo.sellItem(state, itemId, quantity)
        handleResult(result) {}
    }

    fun forage() = withCurrentState { state ->
        val result = repo.forage(state)
        handleResult(result) {}
    }

    fun fish() = withCurrentState { state ->
        val result = repo.fish(state)
        handleResult(result) {}
    }

    fun talkToNpc(npcId: NPCId) = withCurrentState { state ->
        val result = repo.talkToNpc(state, npcId)
        handleResult(result) {}
    }

    fun giftNpc(npcId: NPCId, giftType: ItemType) = withCurrentState { state ->
        val result = repo.giftNpc(state, npcId, giftType)
        handleResult(result) {}
    }

    fun rest() = withCurrentState { state ->
        val (newState, logs) = repo.rest(state)
        _state.value = newState
        logs.firstOrNull()?.let { _snackbar.value = it.message }
    }

    fun clearSnackbar() { _snackbar.value = null }
    fun clearDialogue() { _dialogue.value = null }

    // ── Private helpers ─────────────────────────────────────

    private inline fun withCurrentState(crossinline block: suspend (GameState) -> Unit) {
        viewModelScope.launch {
            _state.value?.let { block(it) }
        }
    }

    private inline fun handleResult(
        result: Result<GameState>,
        crossinline onSuccess: () -> Unit
    ) {
        result.onSuccess { newState ->
            _state.value = newState
            onSuccess()
        }.onFailure { e ->
            _snackbar.value = e.message ?: "Something went wrong"
        }
    }
}

/** Simple wrapper for NPC dialogue events. */
data class NpcDialogue(val npcId: NPCId, val text: String, val hearts: Int)
