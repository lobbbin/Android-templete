package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.example.game.viewmodel.PresidentialViewModel
import com.example.ui.theme.PresidentialTheme
import com.example.ui.screens.AISettingsScreen
import com.example.ui.screens.saveAIConfig
import com.example.ui.screens.loadAIConfig
import com.example.ai.AIConfig

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PresidentialTheme {
                val viewModel: PresidentialViewModel = viewModel()
                val context = LocalContext.current
                
                // Load AI config on startup
                val aiConfig = remember { loadAIConfig(context) }
                LaunchedEffect(Unit) {
                    viewModel.configureAI(aiConfig)
                }
                
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(
                        viewModel = viewModel,
                        onAISettingsClick = { viewModel.toggleAISettings(true) },
                        onSaveAIConfig = { config ->
                            viewModel.configureAI(config)
                            saveAIConfig(context, config)
                            viewModel.toggleAISettings(false)
                        },
                        onBackFromAI = { viewModel.toggleAISettings(false) },
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    viewModel: PresidentialViewModel,
    onAISettingsClick: () -> Unit,
    onSaveAIConfig: (AIConfig) -> Unit,
    onBackFromAI: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val snackbar by viewModel.snackbar.collectAsState()
    val showAISettings by viewModel.showAISettings.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(snackbar) {
        snackbar?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }
    
    if (showAISettings) {
        val context = LocalContext.current
        val currentConfig = remember { loadAIConfig(context) }
        var tempApiKey by remember { mutableStateOf(currentConfig.apiKey) }
        var tempApiEndpoint by remember { mutableStateOf(currentConfig.apiEndpoint) }
        var tempModel by remember { mutableStateOf(currentConfig.model) }
        var tempEnabled by remember { mutableStateOf(currentConfig.enabled) }
        var tempAutoPlay by remember { mutableStateOf(currentConfig.autoPlay) }
        
        AISettingsScreen(
            config = currentConfig.copy(
                apiKey = tempApiKey,
                apiEndpoint = tempApiEndpoint,
                model = tempModel,
                enabled = tempEnabled,
                autoPlay = tempAutoPlay,
            ),
            onConfigChange = { newConfig ->
                tempApiKey = newConfig.apiKey
                tempApiEndpoint = newConfig.apiEndpoint
                tempModel = newConfig.model
                tempEnabled = newConfig.enabled
                tempAutoPlay = newConfig.autoPlay
            },
            onBack = onBackFromAI,
            onSave = {
                onSaveAIConfig(
                    AIConfig(
                        apiKey = tempApiKey,
                        apiEndpoint = tempApiEndpoint,
                        model = tempModel,
                        enabled = tempEnabled,
                        autoPlay = tempAutoPlay,
                    )
                )
            },
        )
        return
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // President header
            state?.let { gameState ->
                val president = gameState.president
                Surface(
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(
                                    "🏛️ ${president.presidentName}",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleLarge,
                                )
                                Text(
                                    "President of ${president.countryName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                            
                            // AI Settings button
                            IconButton(onClick = onAISettingsClick) {
                                Text("🤖", fontSize = androidx.compose.ui.unit.TextUnit(24))
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            StatBox("📊 Approval", "${president.approvalRating}%")
                            StatBox("💰 Treasury", "$${president.treasury}M")
                            StatBox("📈 GDP", "${president.gdpGrowth}%")
                            StatBox("📅 Term ${president.term}", "${president.quarter.emoji} Y${president.year}")
                        }
                    }
                }
                
                // Quick actions
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = { viewModel.advanceDay() },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("⏩ Next Day")
                    }
                    OutlinedButton(
                        onClick = { /* Open policies */ },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("📜 Policies")
                    }
                }
                
                if (viewModel.aiConfig.enabled) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "🤖 AI in Control",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                )
                                if (viewModel.aiConfig.autoPlay) {
                                    Text(
                                        "Auto-play active - AI making continuous decisions",
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                } else {
                                    Text(
                                        "Tap button for AI decision",
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                            
                            if (!viewModel.aiConfig.autoPlay) {
                                Button(
                                    onClick = { 
                                        viewModel.viewModelScope.launch {
                                            viewModel.aiMakeDecision()
                                        }
                                    },
                                ) {
                                    Text("🧠 AI Decide")
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        )
    }
}

@Composable
private fun StatBox(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
    }
}