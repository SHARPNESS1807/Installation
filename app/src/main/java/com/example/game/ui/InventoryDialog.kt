package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.game.crafting.CraftingManager
import com.example.game.entities.GameMode
import com.example.game.entities.Player
import com.example.game.world.ItemStack
import com.example.game.world.ItemType

@Composable
fun InventoryDialog(
    player: Player,
    onDismiss: () -> Unit,
    onItemCrafted: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(if (player.gameMode == GameMode.CREATIVE) 1 else 0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xBB000000))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            // Main stone dialog container
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .height(520.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFC6C6C6))
                    .border(3.dp, Color(0xFF373737), RoundedCornerShape(8.dp))
                    .clickable(enabled = false) {}
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header with title and close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (player.gameMode) {
                                GameMode.CREATIVE -> "Creative Inventory"
                                GameMode.HARDCORE -> "Hardcore Inventory"
                                GameMode.SPECTATOR -> "Spectator Overview"
                                GameMode.SURVIVAL -> "Crafting & Inventory"
                            },
                            color = Color(0xFF3F3F3F),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF3F3F3F))
                        }
                    }

                    // Tab selector
                    val tabs = listOf("Inventory", "Creative", "Crafting", "Advancements")
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color(0xFF8F8F8F),
                        contentColor = Color.White,
                        edgePadding = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    when (selectedTab) {
                        0 -> InventoryTabContent(player)
                        1 -> CreativeItemsTabContent(player)
                        2 -> RecipesTabContent(player, onItemCrafted)
                        3 -> AdvancementsTabContent()
                    }
                }
            }
        }
    }
}

@Composable
private fun InventoryTabContent(player: Player) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Main Inventory (27 slots)",
            color = Color(0xFF4A4A4A),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Main 27 inventory slots (indices 9..35)
        LazyVerticalGrid(
            columns = GridCells.Fixed(9),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f)
        ) {
            items((9..35).toList()) { slotIndex ->
                InventorySlotView(
                    stack = player.inventory[slotIndex],
                    isSelected = false,
                    onClick = {
                        // Swap with hotbar
                        val currentHotbar = player.inventory[player.selectedHotbarSlot]
                        player.inventory[player.selectedHotbarSlot] = player.inventory[slotIndex]
                        player.inventory[slotIndex] = currentHotbar
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Hotbar (9 slots - Tap to equip)",
            color = Color(0xFF4A4A4A),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // 9 Hotbar slots (indices 0..8)
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            for (slotIndex in 0..8) {
                InventorySlotView(
                    stack = player.inventory[slotIndex],
                    isSelected = player.selectedHotbarSlot == slotIndex,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        player.selectedHotbarSlot = slotIndex
                    }
                )
            }
        }
    }
}

@Composable
private fun CreativeItemsTabContent(player: Player) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Creative Catalog (Tap item to add 64 to hotbar)",
            color = Color(0xFF4A4A4A),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        val allItems = ItemType.entries
        LazyVerticalGrid(
            columns = GridCells.Adaptive(44.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(allItems) { item ->
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF8B8B8B))
                        .border(2.dp, Color(0xFF373737), RoundedCornerShape(4.dp))
                        .clickable {
                            val count = if (item.isTool) 1 else 64
                            player.inventory[player.selectedHotbarSlot] = ItemStack(item, count)
                        }
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ItemIcon(item = item, size = 32.dp)
                }
            }
        }
    }
}

@Composable
private fun RecipesTabContent(player: Player, onItemCrafted: () -> Unit) {
    val recipes = CraftingManager.recipes

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(recipes) { recipe ->
            val canCraft = CraftingManager.canCraft(recipe, player.inventory)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (canCraft) Color(0xFF558B2F) else Color(0xFF8F8F8F))
                    .border(2.dp, if (canCraft) Color(0xFF33691E) else Color(0xFF555555), RoundedCornerShape(6.dp))
                    .clickable(enabled = canCraft) {
                        if (CraftingManager.craft(recipe, player.inventory)) {
                            onItemCrafted()
                        }
                    }
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF373737))
                            .border(1.dp, Color(0xFF555555), RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        ItemIcon(item = recipe.result.item, size = 26.dp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${recipe.result.count}x ${recipe.result.item.displayName}",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Requires: " + recipe.ingredients.joinToString(", ") { "${it.second}x ${it.first.displayName}" },
                            color = if (canCraft) Color(0xFFDCEDC8) else Color(0xFFD0D0D0),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (canCraft) Color(0xFF7CB342) else Color(0xFF666666))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (canCraft) "Craft" else "Locked",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun InventorySlotView(
    stack: ItemStack?,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(if (isSelected) Color(0xFFB0BEC5) else Color(0xFF8B8B8B))
            .border(
                width = if (isSelected) 2.5.dp else 1.5.dp,
                color = if (isSelected) Color.White else Color(0xFF373737),
                shape = RoundedCornerShape(3.dp)
            )
            .clickable(onClick = onClick)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        if (stack != null) {
            ItemIcon(item = stack.item, size = 26.dp)
            if (stack.count > 1) {
                Text(
                    text = "${stack.count}",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 1.dp, bottom = 0.dp)
                )
            }
        }
    }
}

@Composable
private fun AdvancementsTabContent() {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Java Advancements Tree (${com.example.game.advancements.AdvancementManager.advancements.count { it.unlocked }}/${com.example.game.advancements.AdvancementManager.advancements.size} Completed)",
            color = Color(0xFF4A4A4A),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(com.example.game.advancements.AdvancementManager.advancements) { adv ->
                val isUnlocked = adv.unlocked
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isUnlocked) Color(0xFF2E3D2F) else Color(0xFF757575))
                        .border(
                            2.dp,
                            if (isUnlocked) Color(0xFFFFD700) else Color(0xFF424242),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = adv.icon, fontSize = 24.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = adv.title,
                                    color = if (isUnlocked) Color(0xFFFFEB3B) else Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = if (isUnlocked) "✓ COMPLETED" else "LOCKED",
                                    color = if (isUnlocked) Color(0xFF81C784) else Color(0xFFB0BEC5),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = adv.description,
                                color = if (isUnlocked) Color(0xFFE0E0E0) else Color(0xFFD6D6D6),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
