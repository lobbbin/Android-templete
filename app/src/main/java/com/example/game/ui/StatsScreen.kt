package com.example.game.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.*
import com.example.game.viewmodel.GameViewModel

/**
 * Stats / profile screen — skills, milestones, relationships overview.
 */
@Composable
fun StatsScreen(
    state: GameState,
    viewModel: GameViewModel,
    onBack: () -> Unit,
) {
    val player = state.player

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onBack) { Text("← Back") }
                Text("📊 ${player.farmName}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.width(48.dp))
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ── Farmer profile ───────────────────────────────────
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("🧑‍🌾 ${player.playerName}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Day ${player.day} of ${player.season.emoji} ${player.season.displayName}, Year ${player.year}", fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column { Text("💰 Gold", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text("${player.gold}g", fontWeight = FontWeight.Bold) }
                            Column { Text("⚡ Energy", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text("${player.energy}/${player.maxEnergy}", fontWeight = FontWeight.Bold) }
                            Column { Text("📅 Days", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text("${player.daysPlayed}", fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }

            // ── Lifetime stats ────────────────────────────────────
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("🏆 Lifetime Stats", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        StatRow("🌾 Total Harvests", "${player.totalHarvests}")
                        StatRow("💰 Total Earnings", "${player.totalEarnings}g")
                        StatRow("🌾 Farm Plots", "${state.plots.size}")
                        StatRow("🐄 Animals", "${state.animals.size}")
                        StatRow("📦 Inventory Items", "${state.inventory.sumOf { it.quantity }}")
                    }
                }
            }

            // ── Skills ──────────────────────────────────────────────
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("⭐ Skills", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        SkillBar("🌾 Farming", player.farmingLevel, player.farmingXp)
                        SkillBar("🌿 Foraging", player.foragingLevel, player.foragingXp)
                        SkillBar("🎣 Fishing", player.fishingLevel, player.fishingXp)
                        SkillBar("🐄 Ranching", player.ranchingLevel, player.ranchingXp)
                        SkillBar("🍲 Cooking", player.cookingLevel, player.cookingXp)
                        SkillBar("💬 Social", player.socialLevel, player.socialXp)
                    }
                }
            }

            // ── Relationships ──────────────────────────────────────
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("👥 Relationships", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        NPCId.entries.forEach { npc ->
                            val rel = state.npcRelationships.find { it.npcId == npc }
                            val hearts = rel?.hearts ?: 0
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("${npc.emoji} ${npc.displayName}", fontSize = 13.sp)
                                Text("❤️ " + "♥".repeat(hearts) + "♡".repeat(10 - hearts), fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontSize = 13.sp)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SkillBar(label: String, level: Int, xp: Int) {
    val xpNeeded = 100 * level * level
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text("Lv.$level  ($xp/$xpNeeded XP)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        LinearProgressIndicator(
            progress = { xp.toFloat() / xpNeeded },
            modifier = Modifier.fillMaxWidth().height(6.dp).padding(top = 2.dp),
        )
    }
}
