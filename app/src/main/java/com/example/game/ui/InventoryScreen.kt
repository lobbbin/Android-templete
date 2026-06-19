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
 * Inventory / shop screen — view items, sell for gold.
 */
@Composable
fun InventoryScreen(
    state: GameState,
    viewModel: GameViewModel,
    onBack: () -> Unit,
) {
    val player = state.player
    var selectedCategory by remember { mutableStateOf(ItemCategory.CROP) }
    var showSellDialog by remember { mutableStateOf<InventoryItem?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onBack) { Text("← Back") }
                Text("🎒 Inventory", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("💰 ${player.gold}g", fontWeight = FontWeight.Bold)
            }
        }

        // Category tabs
        ScrollableTabRow(
            selectedTabIndex = selectedCategory.ordinal,
            edgePadding = 8.dp,
        ) {
            ItemCategory.entries.forEach { cat ->
                Tab(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    text = { Text(cat.displayName, fontSize = 12.sp) },
                )
            }
        }

        val filtered = state.inventory.filter { it.itemType.category == selectedCategory }

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterHorizontally) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📦", fontSize = 48.sp)
                    Text("No ${selectedCategory.displayName.lowercase()} yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(filtered) { item ->
                    InventoryItemCard(
                        item = item,
                        onSell = { showSellDialog = item },
                    )
                }
            }
        }
    }

    // ── Sell confirmation dialog ──────────────────────────────
    showSellDialog?.let { item ->
        val sellPrice = (item.itemType.sellPrice * item.quality).toInt()
        AlertDialog(
            onDismissRequest = { showSellDialog = null },
            title = { Text("💰 Sell ${item.itemType.displayName}?") },
            text = {
                Column {
                    Text("Quality: ★${"%.1f".format(item.quality)}")
                    Text("Sell price: ${sellPrice}g each")
                    Text("Quantity: ×${item.quantity}")
                    if (item.quantity > 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Total: ${sellPrice * item.quantity}g", fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { viewModel.sellItem(item.id, 1); showSellDialog = null }) { Text("Sell 1") }
                    if (item.quantity > 1) {
                        TextButton(onClick = { viewModel.sellItem(item.id, item.quantity); showSellDialog = null }) { Text("Sell All") }
                    }
                }
            },
            dismissButton = { TextButton(onClick = { showSellDialog = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun InventoryItemCard(
    item: InventoryItem,
    onSell: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("${item.itemType.emoji} ${item.itemType.displayName} ×${item.quantity}", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("★${"%.1f".format(item.quality)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${item.itemType.sellPrice}g", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
            if (item.itemType.sellPrice > 0) {
                FilledTonalButton(onClick = onSell, contentPadding = PaddingValues(horizontal = 12.dp)) {
                    Text("💰 Sell", fontSize = 12.sp)
                }
            }
        }
    }
}
