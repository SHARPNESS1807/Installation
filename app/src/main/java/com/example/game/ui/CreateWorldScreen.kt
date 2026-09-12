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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.game.audio.SoundManager
import com.example.game.entities.GameMode
import kotlin.random.Random

enum class WorldType(val displayName: String) {
    DEFAULT("Default"),
    SUPERFLAT("Superflat"),
    LARGE_BIOMES("Large Biomes"),
    AMPLIFIED("Amplified")
}

enum class WorldDifficulty(val displayName: String) {
    PEACEFUL("Peaceful"),
    EASY("Easy"),
    NORMAL("Normal"),
    HARD("Hard")
}

@Composable
fun CreateWorldScreen(
    soundManager: SoundManager,
    onCreate: (name: String, seed: Long, mode: GameMode, isFlat: Boolean, version: String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf("GAME") } // "GAME", "WORLD", "MORE"

    // Game Tab States
    var worldName by remember { mutableStateOf("New World") }
    var gameMode by remember { mutableStateOf(GameMode.SURVIVAL) }
    var difficulty by remember { mutableStateOf(WorldDifficulty.NORMAL) }
    var allowCommands by remember { mutableStateOf(false) }

    // World Tab States
    var worldType by remember { mutableStateOf(WorldType.DEFAULT) }
    var seedText by remember { mutableStateOf("") }
    var generateStructures by remember { mutableStateOf(true) }
    var bonusChest by remember { mutableStateOf(false) }

    // Sub-dialogs
    var showCustomizeDialog by remember { mutableStateOf(false) }
    var showGameRulesDialog by remember { mutableStateOf(false) }
    var showExperimentsDialog by remember { mutableStateOf(false) }
    var showDataPacksDialog by remember { mutableStateOf(false) }

    // Game rules state
    var keepInventory by remember { mutableStateOf(false) }
    var doDaylightCycle by remember { mutableStateOf(true) }
    var mobGriefing by remember { mutableStateOf(true) }
    var doMobSpawning by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xEE111111)),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            // Top Navigation Tabs: [Game] [World] [More]
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(34.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                CreateWorldTab(
                    title = "Game",
                    isSelected = activeTab == "GAME",
                    onClick = {
                        soundManager.playClick()
                        activeTab = "GAME"
                    },
                    modifier = Modifier.weight(1f)
                )
                CreateWorldTab(
                    title = "World",
                    isSelected = activeTab == "WORLD",
                    onClick = {
                        soundManager.playClick()
                        activeTab = "WORLD"
                    },
                    modifier = Modifier.weight(1f)
                )
                CreateWorldTab(
                    title = "More",
                    isSelected = activeTab == "MORE",
                    onClick = {
                        soundManager.playClick()
                        activeTab = "MORE"
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Tab Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(0.62f),
                contentAlignment = Alignment.TopCenter
            ) {
                when (activeTab) {
                    "GAME" -> {
                        // Game Tab (Screenshot 3)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "World Name",
                                color = Color(0xFFAAAAAA),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )

                            OutlinedTextField(
                                value = worldName,
                                onValueChange = { worldName = it },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.Black,
                                    unfocusedContainerColor = Color.Black,
                                    focusedBorderColor = Color.White,
                                    unfocusedBorderColor = Color(0xFFAAAAAA)
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 14.sp
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            )

                            MinecraftGuiButton(
                                text = "Game Mode: ${gameMode.displayName}",
                                onClick = {
                                    soundManager.playClick()
                                    gameMode = when (gameMode) {
                                        GameMode.SURVIVAL -> GameMode.HARDCORE
                                        GameMode.HARDCORE -> GameMode.CREATIVE
                                        GameMode.CREATIVE -> GameMode.SPECTATOR
                                        GameMode.SPECTATOR -> GameMode.SURVIVAL
                                    }
                                }
                            )

                            MinecraftGuiButton(
                                text = "Difficulty: ${difficulty.displayName}",
                                onClick = {
                                    soundManager.playClick()
                                    difficulty = when (difficulty) {
                                        WorldDifficulty.PEACEFUL -> WorldDifficulty.EASY
                                        WorldDifficulty.EASY -> WorldDifficulty.NORMAL
                                        WorldDifficulty.NORMAL -> WorldDifficulty.HARD
                                        WorldDifficulty.HARD -> WorldDifficulty.PEACEFUL
                                    }
                                }
                            )

                            MinecraftGuiButton(
                                text = "Allow Commands: ${if (allowCommands) "ON" else "OFF"}",
                                onClick = {
                                    soundManager.playClick()
                                    allowCommands = !allowCommands
                                }
                            )
                        }
                    }

                    "WORLD" -> {
                        // World Tab (Screenshot 1)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                MinecraftGuiButton(
                                    text = "World Type: ${worldType.displayName}",
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        soundManager.playClick()
                                        worldType = when (worldType) {
                                            WorldType.DEFAULT -> WorldType.SUPERFLAT
                                            WorldType.SUPERFLAT -> WorldType.LARGE_BIOMES
                                            WorldType.LARGE_BIOMES -> WorldType.AMPLIFIED
                                            WorldType.AMPLIFIED -> WorldType.DEFAULT
                                        }
                                    }
                                )
                                MinecraftGuiButton(
                                    text = "Customize",
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        soundManager.playClick()
                                        showCustomizeDialog = true
                                    }
                                )
                            }

                            Text(
                                text = "Seed for the world generator",
                                color = Color(0xFFAAAAAA),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            OutlinedTextField(
                                value = seedText,
                                onValueChange = { seedText = it },
                                placeholder = {
                                    Text(
                                        text = "Leave blank for a random seed",
                                        color = Color(0xFF666666),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 13.sp
                                    )
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.Black,
                                    unfocusedContainerColor = Color.Black,
                                    focusedBorderColor = Color.White,
                                    unfocusedBorderColor = Color(0xFFAAAAAA)
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Generate Structures",
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp
                                )
                                MinecraftGuiButton(
                                    text = if (generateStructures) "ON" else "OFF",
                                    modifier = Modifier.width(96.dp),
                                    onClick = {
                                        soundManager.playClick()
                                        generateStructures = !generateStructures
                                    }
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Bonus Chest",
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp
                                )
                                MinecraftGuiButton(
                                    text = if (bonusChest) "ON" else "OFF",
                                    modifier = Modifier.width(96.dp),
                                    onClick = {
                                        soundManager.playClick()
                                        bonusChest = !bonusChest
                                    }
                                )
                            }
                        }
                    }

                    "MORE" -> {
                        // More Tab (Screenshot 2)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            MinecraftGuiButton(
                                text = "Game Rules",
                                onClick = {
                                    soundManager.playClick()
                                    showGameRulesDialog = true
                                }
                            )

                            MinecraftGuiButton(
                                text = "Experiments",
                                onClick = {
                                    soundManager.playClick()
                                    showExperimentsDialog = true
                                }
                            )

                            MinecraftGuiButton(
                                text = "Data Packs",
                                onClick = {
                                    soundManager.playClick()
                                    showDataPacksDialog = true
                                }
                            )
                        }
                    }
                }
            }

            // Bottom Buttons Row (Screenshot 1, 2, 3)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth(0.62f)
                    .padding(bottom = 6.dp)
            ) {
                MinecraftGuiButton(
                    text = "Create New World",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        soundManager.playClick()
                        val finalSeed = seedText.toLongOrNull() ?: Random.nextLong(100000, 999999)
                        val isFlat = worldType == WorldType.SUPERFLAT
                        onCreate(worldName.ifBlank { "New World" }, finalSeed, gameMode, isFlat, "26.2")
                    }
                )

                MinecraftGuiButton(
                    text = "Cancel",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        soundManager.playClick()
                        onCancel()
                    }
                )
            }
        }

        // Sub Dialogs
        if (showCustomizeDialog) {
            WorldCustomizeDialog(
                worldType = worldType,
                onDismiss = { showCustomizeDialog = false }
            )
        }

        if (showGameRulesDialog) {
            GameRulesDialog(
                keepInventory = keepInventory,
                onKeepInventoryChange = { keepInventory = it },
                doDaylightCycle = doDaylightCycle,
                onDoDaylightCycleChange = { doDaylightCycle = it },
                mobGriefing = mobGriefing,
                onMobGriefingChange = { mobGriefing = it },
                doMobSpawning = doMobSpawning,
                onDoMobSpawningChange = { doMobSpawning = it },
                onDismiss = { showGameRulesDialog = false }
            )
        }

        if (showExperimentsDialog) {
            ExperimentsDialog(onDismiss = { showExperimentsDialog = false })
        }

        if (showDataPacksDialog) {
            DataPacksDialog(onDismiss = { showDataPacksDialog = false })
        }
    }
}

@Composable
fun CreateWorldTab(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isSelected) Color(0x33FFFFFF) else Color(0x22000000))
            .border(
                width = 1.dp,
                color = if (isSelected) Color.White else Color(0x44FFFFFF)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = if (isSelected) Color.White else Color(0xFFAAAAAA),
                fontFamily = FontFamily.Monospace,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp
            )
            if (isSelected) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(2.dp)
                        .background(Color.White)
                )
            }
        }
    }
}

@Composable
fun MinecraftGuiButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp)
            .background(Color(0xFF555555))
            .border(2.dp, Color(0xFF999999))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) Color.White else Color(0xFFAAAAAA),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorldCustomizeDialog(
    worldType: WorldType,
    onDismiss: () -> Unit
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(360.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF222222))
                .border(2.dp, Color(0xFF666666), RoundedCornerShape(4.dp))
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Customize: ${worldType.displayName}",
                    color = Color(0xFFFFD54F),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when (worldType) {
                        WorldType.SUPERFLAT -> "Preset: Classic Flat\n1x Bedrock, 2x Dirt, 1x Grass Block\nBiomes: Plains"
                        WorldType.LARGE_BIOMES -> "Biome scale: 4x larger biomes\nExpansive oceans, massive mountain ranges."
                        WorldType.AMPLIFIED -> "Maximum terrain height variance: peaks up to Y=256, colossal overhangs."
                        WorldType.DEFAULT -> "Default vanilla generation: hills, valleys, rivers, caves, sulfur caverns."
                    },
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
                MinecraftGuiButton(text = "Done", onClick = onDismiss)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameRulesDialog(
    keepInventory: Boolean,
    onKeepInventoryChange: (Boolean) -> Unit,
    doDaylightCycle: Boolean,
    onDoDaylightCycleChange: (Boolean) -> Unit,
    mobGriefing: Boolean,
    onMobGriefingChange: (Boolean) -> Unit,
    doMobSpawning: Boolean,
    onDoMobSpawningChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(380.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF222222))
                .border(2.dp, Color(0xFF666666), RoundedCornerShape(4.dp))
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Edit Game Rules",
                    color = Color(0xFFFFD54F),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                RuleRow(name = "keepInventory", checked = keepInventory, onChecked = onKeepInventoryChange)
                RuleRow(name = "doDaylightCycle", checked = doDaylightCycle, onChecked = onDoDaylightCycleChange)
                RuleRow(name = "mobGriefing", checked = mobGriefing, onChecked = onMobGriefingChange)
                RuleRow(name = "doMobSpawning", checked = doMobSpawning, onChecked = onDoMobSpawningChange)

                Spacer(modifier = Modifier.height(4.dp))
                MinecraftGuiButton(text = "Done", onClick = onDismiss)
            }
        }
    }
}

@Composable
private fun RuleRow(name: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
        Switch(
            checked = checked,
            onCheckedChange = onChecked,
            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF4CAF50))
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExperimentsDialog(onDismiss: () -> Unit) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(380.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF222222))
                .border(2.dp, Color(0xFF666666), RoundedCornerShape(4.dp))
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Minecraft 26.2 Experiments",
                    color = Color(0xFFFFD54F),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "• The Auto-Crafter Block (Active)\n• Mace Weapon Smash Attack (Active)\n• Wind Charge Explosives (Active)\n• Trial Chamber Copper & Tuff (Active)\n• 2000 HP Player God Mode (Active)",
                    color = Color(0xFF81C784),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
                MinecraftGuiButton(text = "Done", onClick = onDismiss)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DataPacksDialog(onDismiss: () -> Unit) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(380.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF222222))
                .border(2.dp, Color(0xFF666666), RoundedCornerShape(4.dp))
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Select Data Packs",
                    color = Color(0xFFFFD54F),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Selected (Enabled):\n• [vanilla] The default data pack for Minecraft 26.2\n• [god_health] 2000 HP Player Profile\n\nAvailable:\n• Vanilla Tweaks 26.2\n• Fast Leaf Decay",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
                MinecraftGuiButton(text = "Done", onClick = onDismiss)
            }
        }
    }
}
