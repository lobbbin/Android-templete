package com.example.ui.screens

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ai.AIConfig

/**
 * AI Settings screen — configure API endpoint and key.
 */
@Composable
fun AISettingsScreen(
    config: AIConfig,
    onConfigChange: (AIConfig) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
) {
    var apiKey by remember { mutableStateOf(config.apiKey) }
    var apiEndpoint by remember { mutableStateOf(config.apiEndpoint) }
    var model by remember { mutableStateOf(config.model) }
    var enabled by remember { mutableStateOf(config.enabled) }
    var autoPlay by remember { mutableStateOf(config.autoPlay) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "🤖 AI Game Master",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Let an AI control your presidency! Configure your LLM provider below.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // API Endpoint
        OutlinedTextField(
            value = apiEndpoint,
            onValueChange = { apiEndpoint = it },
            label = { Text("API Endpoint") },
            placeholder = { Text("https://api.openai.com/v1/chat/completions") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // API Key
        OutlinedTextField(
            value = apiKey,
            onValueChange = { apiKey = it },
            label = { Text("API Key") },
            placeholder = { Text("sk-...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Model selector
        OutlinedTextField(
            value = model,
            onValueChange = { model = it },
            label = { Text("Model") },
            placeholder = { Text("gpt-4o") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Toggle: AI Enabled
        SwitchTile(
            title = "Enable AI Control",
            subtitle = "AI can make decisions for you",
            checked = enabled,
            onCheckedChange = { enabled = it },
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Toggle: Auto-play
        SwitchTile(
            title = "Auto-play Mode",
            subtitle = "AI makes continuous decisions",
            checked = autoPlay && enabled,
            onCheckedChange = { autoPlay = it },
            enabled = enabled,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Example prompt preview
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "📝 How it works",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    """
                    The AI receives the full game state and returns strategic decisions:
                    
                    • Advances time during quiet periods
                    • Enacts policies to boost approval/economy
                    • Resolves crises with optimal choices
                    • Manages diplomacy (aid, war, peace)
                    • Hires/fires cabinet advisors
                    
                    You can override any AI decision or let it run autonomously!
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Save button
        Button(
            onClick = {
                onConfigChange(
                    AIConfig(
                        apiKey = apiKey,
                        apiEndpoint = apiEndpoint,
                        model = model,
                        enabled = enabled,
                        autoPlay = autoPlay,
                    )
                )
                onSave()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = apiEndpoint.isNotEmpty() && apiKey.isNotEmpty(),
        ) {
            Text("💾 Save Configuration")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("← Back to Game")
        }
    }
}

@Composable
private fun SwitchTile(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
    }
}

/**
 * Persist AI config to SharedPreferences.
 */
fun saveAIConfig(context: Context, config: AIConfig) {
    val prefs: SharedPreferences = context.getSharedPreferences("ai_config", Context.MODE_PRIVATE)
    prefs.edit().apply {
        putString("api_key", config.apiKey)
        putString("api_endpoint", config.apiEndpoint)
        putString("model", config.model)
        putBoolean("enabled", config.enabled)
        putBoolean("auto_play", config.autoPlay)
        apply()
    }
}

/**
 * Load AI config from SharedPreferences.
 */
fun loadAIConfig(context: Context): AIConfig {
    val prefs: SharedPreferences = context.getSharedPreferences("ai_config", Context.MODE_PRIVATE)
    return AIConfig(
        apiKey = prefs.getString("api_key", "") ?: "",
        apiEndpoint = prefs.getString("api_endpoint", "") ?: "",
        model = prefs.getString("model", "gpt-4o") ?: "gpt-4o",
        enabled = prefs.getBoolean("enabled", false),
        autoPlay = prefs.getBoolean("auto_play", false),
    )
}