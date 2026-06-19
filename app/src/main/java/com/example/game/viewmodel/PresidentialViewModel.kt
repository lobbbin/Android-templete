package com.example.game.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.game.engine.PresidentialEngine
import com.example.game.model.GameState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PresidentialViewModel : ViewModel() {
    
    private val engine = PresidentialEngine()
    
    private val _state = MutableStateFlow<GameState?>(null)
    val state: StateFlow<GameState?> = _state.asStateFlow()
    
    private val _snackbar = MutableStateFlow<String?>(null)
    val snackbar: StateFlow<String?> = _snackbar.asStateFlow()
    
    init {
        // Start new game on init
        startNewGame("Mr. President", "United States")
    }
    
    fun startNewGame(presidentName: String, countryName: String) {
        _state.value = engine.newGame(presidentName, countryName)
    }
    
    fun advanceDay() {
        viewModelScope.launch {
            val (quarterEnded, newState) = engine.advanceDay()
            _state.value = newState
            
            if (quarterEnded) {
                _snackbar.value = "Quarter ended! Economic report coming soon..."
            }
        }
    }
    
    fun enactPolicy(policyId: String) {
        viewModelScope.launch {
            val newState = engine.enactPolicy(policyId)
            _state.value = newState
            _snackbar.value = "Policy action taken"
        }
    }
    
    fun resolveCrisis(crisisId: String, optionId: String) {
        viewModelScope.launch {
            val newState = engine.resolveCrisis(crisisId, optionId)
            _state.value = newState
            _snackbar.value = "Crisis resolved"
        }
    }
    
    fun sendAid(nationId: String, amount: Int) {
        viewModelScope.launch {
            val newState = engine.improveRelations(nationId, amount)
            _state.value = newState
            _snackbar.value = "Foreign aid sent"
        }
    }
    
    fun clearSnackbar() {
        _snackbar.value = null
    }
    
    fun isGameOver(): Boolean = engine.isGameOver()
}