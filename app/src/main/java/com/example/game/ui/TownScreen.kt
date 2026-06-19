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
import com.example.game.viewmodel.GameViewModel

/**
 * Town hub — forage, fish, visit NPCs, buy animals.
 */
@Composable
fun TownScreen(
    state: GameState,
    viewModel: GameViewModel,
    onBack: () -> Unit,
) {
    val player = state.player
    var showNpcDetail by remember { mutableStateOf<NPCId?>(null) }
    var showAnimalShop by remember { mutableStateOf(false) }
    var showGiftPicker by remember { mutableStateOf<NPCId?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onBack) { Text("← Farm") }
                Text("🏘️ Town", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("💰 ${player.gold}g", fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // ── Quick actions ─────────────────────────────────────
            item {
                Text("🌿 Wilds", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ElevatedCard(modifier = Modifier.weight(1f).clickable { viewModel.forage() }) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🌿 Forage", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Search the wilds for herbs & berries", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("8⚡", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    ElevatedCard(modifier = Modifier.weight(1f).clickable { viewModel.fish() }) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🎣 Fish", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Cast your line in the stream", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("10⚡", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // ── Animal shop ────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("🐄 Animal Shop", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            item {
                AnimalShopCard(
                    gold = player.gold,
                    onBuy = { type, name -> viewModel.buyAnimal(type, name) },
                )
            }

            // ── Feed & collect ─────────────────────────────────────
            if (state.animals.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(onClick = { viewModel.feedAnimals() }, modifier = Modifier.weight(1f)) {
                            Text("🍽️ Feed All", fontSize = 12.sp)
                        }
                    }
                }
                items(state.animals) { animal ->
                    AnimalCard(
                        animal = animal,
                        onCollect = { viewModel.collectProduct(animal.id) },
                    )
                }
            }

            // ── Villagers ──────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text("👥 Villagers", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            items(NPCId.entries) { npc ->
                val rel = state.npcRelationships.find { it.npcId == npc }
                NpcCard(
                    npc = npc,
                    hearts = rel?.hearts ?: 0,
                    talkedToday = rel?.talkedToday ?: false,
                    onTalk = { viewModel.talkToNpc(npc) },
                    onGift = { showGiftPicker = npc },
                )
            }
        }
    }

    // ── Gift picker dialog ────────────────────────────────
    showGiftPicker?.let { npcId ->
        val gifts = state.inventory.filter { it.itemType.category in listOf(ItemCategory.CROP, ItemCategory.FORAGE, ItemCategory.ANIMAL_PRODUCT, ItemCategory.GIFT) }
        AlertDialog(
            onDismissRequest = { showGiftPicker = null },
            title = { Text("🎁 Give gift to ${npcId.displayName}") },
            text = {
                if (gifts.isEmpty()) {
                    Text("No giftable items in your inventory.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(gifts) { item ->
                            val isLiked = item.itemType in npcId.likedGifts
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    viewModel.giftNpc(npcId, item.itemType)
                                    showGiftPicker = null
                                },
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("${item.itemType.emoji} ${item.itemType.displayName} ×${item.quantity}")
                                if (isLiked) Text("❤️ Loves!", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showGiftPicker = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun AnimalShopCard(gold: Int, onBuy: (AnimalType, String) -> Unit) {
    var showPicker by remember { mutableStateOf<AnimalType?>(null) }
    var animalName by remember { mutableStateOf("") }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AnimalType.entries.forEach { type ->
            ElevatedCard(
                modifier = Modifier.weight(1f).clickable(enabled = gold >= type.buyPrice) { showPicker = type },
            ) {
                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(type.emoji, fontSize = 24.sp)
                    Text(type.displayName, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("${type.buyPrice}g", fontSize = 12.sp, color = if (gold >= type.buyPrice) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                    Text("+${type.product.displayName}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    showPicker?.let { type ->
        AlertDialog(
            onDismissRequest = { showPicker = null; animalName = "" },
            title = { Text("Buy ${type.displayName} for ${type.buyPrice}g?") },
            text = {
                OutlinedTextField(
                    value = animalName,
                    onValueChange = { animalName = it },
                    label = { Text("Name your ${type.displayName}") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onBuy(type, animalName.ifBlank { type.displayName })
                    showPicker = null
                    animalName = ""
                }) { Text("Buy") }
            },
            dismissButton = { TextButton(onClick = { showPicker = null; animalName = "" }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun AnimalCard(animal: Animal, onCollect: () -> Unit) {
    val productReady = animal.isFed && animal.daysSinceProduct >= animal.type.productDays
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("${animal.type.emoji} ${animal.name}", fontWeight = FontWeight.Bold)
                Text("Happiness: ${"❤️".repeat((animal.happiness / 20).coerceIn(0, 5))}", fontSize = 12.sp)
                if (!animal.isFed) Text("Hungry! 🍽️", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                else Text("Fed ✓", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
            if (productReady) {
                Button(onClick = onCollect) { Text("🧺 Collect", fontSize = 12.sp) }
            } else if (animal.isFed) {
                Text("Next: ${animal.type.productDays - animal.daysSinceProduct}d", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun NpcCard(
    npc: NPCId,
    hearts: Int,
    talkedToday: Boolean,
    onTalk: () -> Unit,
    onGift: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("${npc.emoji} ${npc.displayName}", fontWeight = FontWeight.Bold)
                    Text(npc.role, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("❤️ " + "♥".repeat(hearts) + "♡".repeat(10 - hearts), fontSize = 12.sp)
                    if (talkedToday) Text("💬 Talked today", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (!talkedToday) {
                        OutlinedButton(onClick = onTalk, contentPadding = PaddingValues(horizontal = 8.dp)) {
                            Text("💬 Talk", fontSize = 11.sp)
                        }
                    }
                    OutlinedButton(onClick = onGift, contentPadding = PaddingValues(horizontal = 8.dp)) {
                        Text("🎁 Gift", fontSize = 11.sp)
                    }
                }
            }
            Text(npc.personality, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
