package com.example.game.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.*

/**
 * Main HUD + farm view. Shows date/weather bar, energy, gold, farm grid, and action buttons.
 */
@Composable
fun FarmScreen(
    state: GameState,
    viewModel: GameViewModel,
    onNavigateToTown: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToStats: () -> Unit,
) {
    val player = state.player
    var selectedPlotId by remember { mutableStateOf<Long?>(null) }
    var showCropPicker by remember { mutableStateOf(false) }
    var showPlotActions by remember { mutableLongStateOf(-1L) }

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Top bar: Date / Weather / Gold / Energy ───────────
        TopBar(state, viewModel)

        // ── Farm plot grid ─────────────────────────────────────
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item {
                Text(
                    "🗺️ Your Farm — ${player.farmName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }

            items(state.plots.chunked(3)) { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    row.forEach { plot ->
                        PlotCard(
                            plot = plot,
                            isSelected = selectedPlotId == plot.id,
                            onSelect = { selectedPlotId = plot.id },
                            onAction = { action ->
                                when (action) {
                                    "till" -> viewModel.tillPlot(plot.id)
                                    "plant" -> { showCropPicker = true; selectedPlotId = plot.id }
                                    "water" -> viewModel.waterPlot(plot.id)
                                    "harvest" -> viewModel.harvestPlot(plot.id)
                                }
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    // Pad if row has fewer than 3
                    repeat(3 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // ── Batch action buttons ──────────────────────────
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(onClick = { viewModel.waterAllPlots() }, modifier = Modifier.weight(1f)) {
                        Text("💧 Water All", fontSize = 12.sp)
                    }
                    OutlinedButton(onClick = { viewModel.harvestAllReady() }, modifier = Modifier.weight(1f)) {
                        Text("🌾 Harvest All", fontSize = 12.sp)
                    }
                }
            }

            // ── Event log ────────────────────────────────────────
            item {
                if (state.logMessages.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("📜 Event Log", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            items(state.logMessages.take(5)) { msg ->
                Text(
                    text = "${msg.emoji} ${msg.message}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, bottom = 2.dp),
                )
            }
        }

        // ── Bottom navigation bar ──────────────────────────────
        BottomBar(viewModel, onNavigateToTown, onNavigateToInventory, onNavigateToStats)
    }

    // ── Crop picker dialog ─────────────────────────────────
    if (showCropPicker && selectedPlotId != null) {
        CropPickerDialog(
            season = player.season,
            gold = player.gold,
            onPick = { crop ->
                selectedPlotId?.let { viewModel.plantCrop(it, crop) }
                showCropPicker = false
            },
            onDismiss = { showCropPicker = false },
        )
    }
}

// ════════════════════════════════════════════════════════════
// Sub-components
// ════════════════════════════════════════════════════════════

@Composable
private fun TopBar(state: GameState, viewModel: GameViewModel) {
    val p = state.player
    Surface(
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("📅 Day ${p.day} — ${p.season.emoji} ${p.season.displayName}, Year ${p.year}", fontWeight = FontWeight.Bold)
                Text("💰 ${p.gold}g", fontWeight = FontWeight.Bold)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("${p.weather.emoji} ${p.weather.displayName}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("⚡ ${p.energy}/${p.maxEnergy}", fontSize = 13.sp)
            }
            // Energy bar
            LinearProgressIndicator(
                progress = { p.energy.toFloat() / p.maxEnergy },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp).height(6.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(onClick = { viewModel.advanceDay() }, modifier = Modifier.weight(1f)) {
                    Text("🌅 Sleep / Next Day")
                }
                OutlinedButton(onClick = { viewModel.rest() }, modifier = Modifier.weight(1f)) {
                    Text("😴 Rest (+30⚡)")
                }
            }
        }
    }
}

@Composable
private fun PlotCard(
    plot: FarmPlot,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onAction: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor = when (isSelected) {
        true -> MaterialTheme.colorScheme.primaryContainer
        false -> MaterialTheme.colorScheme.surfaceVariant
    }
    Card(
        modifier = modifier.height(100.dp).clickable { onSelect() },
        colors = CardDefaults.cardColors(containerColor = bgColor),
    ) {
        Column(
            modifier = Modifier.padding(8.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "${plot.status.emoji} ${plot.status.displayName}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
            if (plot.crop != null) {
                Text(
                    "${plot.crop.displayName} (${plot.daysPlanted}/${plot.crop.growDays}d)",
                    fontSize = 11.sp,
                )
                if (plot.isWatered) {
                    Text("💧 Watered", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                }
            }

            // Action button
            when (plot.status) {
                PlotStatus.EMPTY -> {
                    TextButton(onClick = { onAction("till") }, contentPadding = PaddingValues(0.dp)) {
                        Text("⛏️ Till", fontSize = 11.sp)
                    }
                }
                PlotStatus.TILLED -> {
                    TextButton(onClick = { onAction("plant") }, contentPadding = PaddingValues(0.dp)) {
                        Text("🌱 Plant", fontSize = 11.sp)
                    }
                }
                PlotStatus.PLANTED, PlotStatus.GROWING -> {
                    if (!plot.isWatered) {
                        TextButton(onClick = { onAction("water") }, contentPadding = PaddingValues(0.dp)) {
                            Text("💧 Water", fontSize = 11.sp)
                        }
                    }
                }
                PlotStatus.READY -> {
                    TextButton(onClick = { onAction("harvest") }, contentPadding = PaddingValues(0.dp)) {
                        Text("🌾 Harvest", fontSize = 11.sp)
                    }
                }
                PlotStatus.WITHERED -> {
                    TextButton(onClick = { onAction("till") }, contentPadding = PaddingValues(0.dp)) {
                        Text("🔄 Clear", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun CropPickerDialog(
    season: Season,
    gold: Int,
    onPick: (CropType) -> Unit,
    onDismiss: () -> Unit,
) {
    val crops = CropType.availableIn(season)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🌱 Plant Crop (${season.emoji} ${season.displayName})") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(crops) { crop ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable(enabled = gold >= crop.buyPrice) { onPick(crop) },
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            "${crop.displayName} (${crop.growDays}d) — ${crop.buyPrice}g",
                            fontSize = 14.sp,
                            color = if (gold >= crop.buyPrice) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        )
                        Text("→ ${crop.sellPrice}g", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun BottomBar(
    viewModel: GameViewModel,
    onNavigateToTown: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToStats: () -> Unit,
) {
    Surface(
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(onClick = onNavigateToTown, modifier = Modifier.weight(1f)) {
                Text("🏘️ Town", fontSize = 12.sp)
            }
            OutlinedButton(onClick = onNavigateToInventory, modifier = Modifier.weight(1f)) {
                Text("🎒 Items", fontSize = 12.sp)
            }
            OutlinedButton(onClick = onNavigateToStats, modifier = Modifier.weight(1f)) {
                Text("📊 Stats", fontSize = 12.sp)
            }
        }
    }
}
