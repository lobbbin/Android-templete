package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.game.model.GameState
import com.example.game.ui.FarmScreen
import com.example.game.ui.InventoryScreen
import com.example.game.ui.MainMenuScreen
import com.example.game.ui.StatsScreen
import com.example.game.ui.TownScreen
import com.example.game.viewmodel.GameViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: GameViewModel = viewModel()
                val state by viewModel.state.collectAsState()

                CountryLifeApp(viewModel = viewModel, state = state)
            }
        }
    }
}

@Composable
fun CountryLifeApp(viewModel: GameViewModel, state: GameState?) {
    val navController = rememberNavController()

    // If no game state, show main menu
    if (state == null) {
        MainMenuScreen(
            viewModel = viewModel,
            onGameStarted = { /* state will be non-null on next composition */ },
        )
        return
    }

    // Snackbar for feedback messages
    val snackbar by viewModel.snackbar.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar when message changes
    LaunchedEffect(snackbar) {
        snackbar?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = "farm",
        ) {
            composable("farm") {
                FarmScreen(
                    state = state,
                    viewModel = viewModel,
                    onNavigateToTown = { navController.navigate("town") },
                    onNavigateToInventory = { navController.navigate("inventory") },
                    onNavigateToStats = { navController.navigate("stats") },
                )
            }
            composable("town") {
                TownScreen(
                    state = state,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                )
            }
            composable("inventory") {
                InventoryScreen(
                    state = state,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                )
            }
            composable("stats") {
                StatsScreen(
                    state = state,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                )
            }
        }

        // Snackbar overlay
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp),
        )
    }
}
