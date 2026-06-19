package com.example.game.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AIConfig
import com.example.ai.AIGameAgent
import com.example.ai.AIGameAgent.AIAction
import com.example.game.engine.PresidentialEngine
import com.example.game.model.GameState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Presidential game ViewModel with AI integration.
 */
class PresidentialViewModel : ViewModel() {
    private val engine = PresidentialEngine()
    
    private val _state = MutableStateFlow<GameState?>(null)
    val state: StateFlow<GameState?> = _state.asStateFlow()
    
    private val _snackbar = MutableStateFlow<String?>(null)
    val snackbar: StateFlow<String?> = _snackbar.asStateFlow()
    
    private val _showAISettings = MutableStateFlow(false)
    val showAISettings: StateFlow<Boolean> = _showAISettings.asStateFlow()
    
    internal var aiConfig = AIConfig()
        private set
    private var aiAgent: AIGameAgent? = null

    init {
        startNewGame()
    }

    fun startNewGame() {
        engine.newGame()
        _state.value = engine.getGameState()
        showSnackbar("New term begins! Good luck, Mr. President!")
    }

    fun loadGame() {
        // TODO: Implement save/load from Room database
        engine.newGame()
        _state.value = engine.getGameState()
    }

    fun saveGame() {
        // TODO: Implement save to Room database
        showSnackbar("Game saved!")
    }

    // ── Player actions ────────────────────────────────────────────

    fun advanceDay(days: Int = 1) {
        val result = engine.advanceDays(days)
        _state.value = result.second
        if (result.first) {
            showSnackbar("Advanced $days day(s)")
        }
    }

    fun enactPolicy(policyId: String) {
        val newState = engine.enactPolicy(policyId)
        _state.value = newState
        showSnackbar("Policy enacted!")
    }

    fun sendAid(nationId: String, amount: Int) {
        val newState = engine.sendAid(nationId, amount)
        _state.value = newState
    }

    fun declareWar(nationId: String) {
        val newState = engine.declareWar(nationId)
        _state.value = newState
        showSnackbar("War declared!")
    }

    fun signPeaceTreaty(nationId: String) {
        val newState = engine.signPeaceTreaty(nationId)
        _state.value = newState
    }

    fun resolveCrisis(crisisId: String, optionId: String) {
        val newState = engine.resolveCrisis(crisisId, optionId)
        _state.value = newState
    }

    fun hireAdvisor(categoryId: String, name: String) {
        val newState = engine.hireAdvisor(categoryId, name)
        _state.value = newState
    }

    fun fireAdvisor(categoryId: String) {
        val newState = engine.fireAdvisor(categoryId)
        _state.value = newState
    }

    // ── AI Integration ───────────────────────────────────────────

    fun configureAI(config: AIConfig) {
        aiConfig = config
        aiAgent = AIGameAgent(config) { action ->
            handleAIAction(action)
        }
    }

    fun toggleAISettings(show: Boolean) {
        _showAISettings.value = show
    }

    suspend fun aiMakeDecision() {
        val currentState = _state.value ?: return
        if (!aiConfig.enabled || aiConfig.apiKey.isEmpty() || aiConfig.apiEndpoint.isEmpty()) {
            showSnackbar("⚠️ Configure AI settings first!")
            return
        }
        
        val agent = aiAgent ?: return
        
        try {
            agent.makeDecision(currentState)
        } catch (e: Exception) {
            showSnackbar("❌ AI error: ${e.message}")
        }
    }

    private fun handleAIAction(action: AIAction) {
        when (action) {
            is AIAction.AdvanceDay -> advanceDay(action.days)
            is AIAction.EnactPolicy -> enactPolicy(action.policyId)
            is AIAction.SendAid -> sendAid(action.nationId, action.amount)
            is AIAction.DeclareWar -> declareWar(action.nationId)
            is AIAction.SignPeaceTreaty -> signPeaceTreaty(action.nationId)
            is AIAction.ResolveCrisis -> resolveCrisis(action.crisisId, action.optionId)
            is AIAction.HireAdvisor -> hireAdvisor(action.categoryId, action.name)
            is AIAction.FireAdvisor -> fireAdvisor(action.categoryId)
            AIAction.DoNothing -> {
                // AI chose to wait
            }
        }

        viewModelScope.launch {
            // If auto-play, schedule next decision
            if (aiConfig.autoPlay && aiConfig.enabled) {
                kotlinx.coroutines.delay(2000) // 2 second pause between decisions
                aiMakeDecision()
            }
        }
    }

    // ── Internal helpers ─────────────────────────────────────────

    private fun showSnackbar(message: String) {
        _snackbar.value = message
    }

    fun clearSnackbar() {
        _snackbar.value = null
    }
}