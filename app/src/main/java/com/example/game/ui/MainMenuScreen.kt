package com.example.game.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.GameState
import com.example.game.viewmodel.GameViewModel

/**
 * Title / new-game screen. Shown when there's no saved game.
 */
@Composable
fun MainMenuScreen(
    viewModel: GameViewModel,
    onGameStarted: () -> Unit,
) {
    var playerName by remember { mutableStateOf("Farmer") }
    var farmName by remember { mutableStateOf("Green Acres") }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "🏡",
            fontSize = 64.sp,
        )
        Text(
            text = "Country Life",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "A Text-Based Life Simulator",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = playerName,
            onValueChange = { playerName = it },
            label = { Text("Your Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = farmName,
            onValueChange = { farmName = it },
            label = { Text("Farm Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.startNewGame(playerName.ifBlank { "Farmer" }, farmName.ifBlank { "Green Acres" })
                onGameStarted()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("🌾 Start New Life", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Flavor text
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Welcome to Country Life!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "You've just inherited a small farm on the outskirts of a quiet country town. " +
                    "Plant crops, raise animals, forage in the wilds, fish the streams, and " +
                    "befriend the villagers. Each day is a chance to grow — your farm, your skills, " +
                    "and your story.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
