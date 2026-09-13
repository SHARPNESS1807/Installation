package com.example.game.ui

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.audio.SoundManager
import com.example.game.entities.GameMode
import com.example.game.persistence.WorldMetadata
import com.example.game.version.VersionManager
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TitleScreen(
    savedWorlds: List<WorldMetadata>,
    soundManager: SoundManager,
    versionManager: VersionManager,
    fov: Float,
    onFovChange: (Float) -> Unit,
    sensitivity: Float,
    onSensitivityChange: (Float) -> Unit,
    onPlayWorld: (WorldMetadata) -> Unit,
    onCreateWorld: (name: String, seed: Long, mode: GameMode, isFlat: Boolean, version: String) -> Unit,
    onDeleteWorld: (String) -> Unit
) {
    val context = LocalContext.current
    var screenState by remember { mutableStateOf("MAIN") } // "MAIN", "WORLDS", "CREATE", "OPTIONS", "ABOUT", "MARKETPLACE", "MULTIPLAYER", "CREDITS"

    // Dialogs
    var showRealmsDialog by remember { mutableStateOf(false) }
    var showPartyDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showAccessibilityDialog by remember { mutableStateOf(false) }
    var showKeyboardDialog by remember { mutableStateOf(false) }

    // On-screen notification / toast from controller buttons
    var toastText by remember { mutableStateOf("") }
    var showToast by remember { mutableStateOf(false) }

    fun notify(msg: String) {
        toastText = msg
        showToast = true
    }

    LaunchedEffect(showToast) {
        if (showToast) {
            delay(1500)
            showToast = false
        }
    }

    val currentSplash = remember { "Stay a while, stay forever!" }

    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "splashScale"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Sulfur Cave Background Image
        Image(
            painter = painterResource(id = R.drawable.img_sulfur_cave),
            contentDescription = "Sulfur Cave Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dark Atmospheric Cave Vignette & Scrim for UI contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x660E0C04),
                            Color(0x880A0803),
                            Color(0xB2060502)
                        )
                    )
                )
        )

        when (screenState) {
            "MAIN" -> {
                // Main Menu
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    // Compact Title Logo Area
                    Box(
                        modifier = Modifier.padding(bottom = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "MINECRAFT",
                                color = Color(0xFFDDDDDD),
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp,
                                modifier = Modifier
                                    .background(Color(0xFF333333), RoundedCornerShape(4.dp))
                                    .border(2.dp, Color(0xFF777777), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 14.dp, vertical = 2.dp)
                            )
                            Text(
                                text = "JAVA EDITION",
                                color = Color(0xFFFFD54F),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 3.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        // Yellow bouncing splash text
                        Text(
                            text = currentSplash,
                            color = Color(0xFFFFEB3B),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .rotate(-12f)
                                .scale(scale)
                                .padding(end = 4.dp)
                        )
                    }

                    // Main Menu Buttons
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.width(310.dp)
                    ) {
                        MinecraftMenuButton(
                            text = "Singleplayer",
                            height = 33.dp,
                            fontSize = 13.sp,
                            onClick = {
                                soundManager.playClick()
                                screenState = "WORLDS"
                            }
                        )

                        MinecraftMenuButton(
                            text = "Multiplayer",
                            height = 33.dp,
                            fontSize = 13.sp,
                            onClick = {
                                soundManager.playClick()
                                screenState = "MULTIPLAYER"
                            }
                        )

                        MinecraftMenuButton(
                            text = "Minecraft Realms",
                            height = 33.dp,
                            fontSize = 13.sp,
                            onClick = {
                                soundManager.playClick()
                                showRealmsDialog = true
                            }
                        )

                        MinecraftMenuButton(
                            text = "Free Marketplace & Mods",
                            height = 33.dp,
                            fontSize = 13.sp,
                            onClick = {
                                soundManager.playClick()
                                screenState = "MARKETPLACE"
                            }
                        )

                        // 3 Square Icon Buttons ON TOP of [Options...] and [Quit Game]
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            MinecraftSquareIconButton(
                                iconText = "👥",
                                size = 30.dp,
                                fontSize = 15.sp,
                                onClick = {
                                    soundManager.playClick()
                                    showPartyDialog = true
                                }
                            )
                            MinecraftSquareIconButton(
                                iconText = "🌐",
                                size = 30.dp,
                                fontSize = 15.sp,
                                onClick = {
                                    soundManager.playClick()
                                    showLanguageDialog = true
                                }
                            )
                            MinecraftSquareIconButton(
                                iconText = "♿",
                                size = 30.dp,
                                fontSize = 15.sp,
                                onClick = {
                                    soundManager.playClick()
                                    showAccessibilityDialog = true
                                }
                            )
                        }

                        // Bottom Row: [Options...] [Quit Game]
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            MinecraftMenuButton(
                                text = "Options...",
                                height = 33.dp,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    soundManager.playClick()
                                    screenState = "OPTIONS"
                                }
                            )
                            MinecraftMenuButton(
                                text = "Quit Game",
                                height = 33.dp,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    soundManager.playClick()
                                    (context as? Activity)?.finish()
                                }
                            )
                        }
                    }
                }
            }

            "WORLDS" -> {
                WorldSelectionScreen(
                    worlds = savedWorlds,
                    onSelectWorld = onPlayWorld,
                    onCreateNew = { screenState = "CREATE" },
                    onDelete = onDeleteWorld,
                    onBack = { screenState = "MAIN" }
                )
            }

            "CREATE" -> {
                CreateWorldScreen(
                    soundManager = soundManager,
                    onCreate = { name, seed, mode, flat, ver ->
                        onCreateWorld(name, seed, mode, flat, ver)
                    },
                    onCancel = { screenState = "WORLDS" }
                )
            }

            "OPTIONS" -> {
                OptionsScreen(
                    soundManager = soundManager,
                    fov = fov,
                    onFovChange = onFovChange,
                    sensitivity = sensitivity,
                    onSensitivityChange = onSensitivityChange,
                    onBack = { screenState = "MAIN" }
                )
            }

            "ABOUT" -> {
                AboutScreen(
                    versionManager = versionManager,
                    onBack = { screenState = "MAIN" }
                )
            }

            "MARKETPLACE" -> {
                MarketplaceScreen(
                    soundManager = soundManager,
                    onLaunchWorld = { name, seed, mode, isFlat ->
                        onCreateWorld(name, seed, mode, isFlat, "26.2")
                    },
                    onBack = { screenState = "MAIN" }
                )
            }

            "MULTIPLAYER" -> {
                MultiplayerScreen(
                    soundManager = soundManager,
                    onJoinServer = onPlayWorld,
                    onBack = { screenState = "MAIN" }
                )
            }

            "CREDITS" -> {
                CreditsVideoScreen(
                    onBack = { screenState = "MAIN" }
                )
            }
        }

        // Watermark & Copyright (Screenshot 4)
        if (screenState == "MAIN") {
            Text(
                text = "Minecraft 26.2",
                color = Color(0xCCFFFFFF),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            )

            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0x99222222))
                        .border(1.dp, Color(0xAA888888), RoundedCornerShape(3.dp))
                        .clickable {
                            soundManager.playClick()
                            screenState = "CREDITS"
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Credits",
                        color = Color(0xFFFFD54F),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Copyright Mojang AB. Do not distribute!",
                    color = Color(0xCCFFFFFF),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    modifier = Modifier.clickable {
                        soundManager.playClick()
                        screenState = "CREDITS"
                    }
                )
            }
        }

        // Controller Notification Toast
        AnimatedVisibility(
            visible = showToast,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xDD000000))
                    .border(1.dp, Color(0xFF81C784), RoundedCornerShape(4.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = toastText,
                    color = Color(0xFF81C784),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Sub-dialogs: Realms, Skins, Language, Accessibility, Keyboard
        if (showRealmsDialog) {
            RealmsDialog(onDismiss = { showRealmsDialog = false })
        }
        if (showPartyDialog) {
            CreatePartyDialog(
                soundManager = soundManager,
                onDismiss = { showPartyDialog = false }
            )
        }
        if (showLanguageDialog) {
            LanguageDialog(onDismiss = { showLanguageDialog = false })
        }
        if (showAccessibilityDialog) {
            AccessibilityDialog(onDismiss = { showAccessibilityDialog = false })
        }
        if (showKeyboardDialog) {
            KeyboardDialog(onDismiss = { showKeyboardDialog = false })
        }
    }
}

@Composable
fun MinecraftSquareIconButton(
    iconText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    fontSize: TextUnit = 14.sp
) {
    Box(
        modifier = modifier
            .size(size)
            .background(Color(0xFF555555))
            .border(1.5.dp, Color(0xFF999999))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = iconText,
            fontSize = fontSize,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldSelectionScreen(
    worlds: List<WorldMetadata>,
    onSelectWorld: (WorldMetadata) -> Unit,
    onCreateNew: () -> Unit,
    onDelete: (String) -> Unit,
    onBack: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("M/d/yy, h:mm a", Locale.getDefault()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedWorldId by remember(worlds) { mutableStateOf(worlds.firstOrNull()?.id) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }

    val filteredWorlds = remember(worlds, searchQuery) {
        if (searchQuery.isBlank()) worlds
        else worlds.filter { it.name.contains(searchQuery, ignoreCase = true) || it.gameMode.displayName.contains(searchQuery, ignoreCase = true) }
    }

    val selectedWorld = remember(filteredWorlds, selectedWorldId) {
        filteredWorlds.firstOrNull { it.id == selectedWorldId } ?: filteredWorlds.firstOrNull()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
    ) {
        // Top Header Bar: Centered "Select World" & Top-Right "MOUSE" button (Screenshot MJLaunch)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(bottom = 8.dp)
        ) {
            Text(
                text = "Select World",
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.align(Alignment.Center)
            )

            // Top-right MOUSE button from Pojav / MJLaunch screenshot
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .height(26.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF2B2B2B))
                    .border(1.2.dp, Color(0xFF888888), RoundedCornerShape(2.dp))
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "MOUSE",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Search Bar (Screenshot MJLaunch)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(bottom = 10.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Search...",
                        color = Color(0xFF777777),
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(Color.Black),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFAAAAAA),
                    unfocusedBorderColor = Color(0xFF666666),
                    focusedContainerColor = Color.Black,
                    unfocusedContainerColor = Color.Black
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
            )
        }

        // Center World List with Dark Frame
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(0.92f)
                .background(Color(0x99000000))
                .border(1.5.dp, Color(0xFF3A3A3A))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredWorlds) { world ->
                    val isSelected = world.id == (selectedWorld?.id)
                    val worldIndex = worlds.indexOf(world) + 1

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isSelected) Color(0xFF1E1E1E) else Color(0x66111111))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color.White else Color(0xFF2E2E2E)
                            )
                            .clickable {
                                if (isSelected) {
                                    onSelectWorld(world)
                                } else {
                                    selectedWorldId = world.id
                                }
                            }
                            .padding(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // 58x58 Landscape Icon Thumbnail with optional play icon
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        when {
                                            world.gameMode == GameMode.HARDCORE -> Brush.verticalGradient(
                                                listOf(Color(0xFF3E120A), Color(0xFF8E2A12), Color(0xFFE65100))
                                            )
                                            world.isFlat -> Brush.verticalGradient(
                                                listOf(Color(0xFF4FC3F7), Color(0xFF81C784), Color(0xFF2E7D32))
                                            )
                                            else -> Brush.verticalGradient(
                                                listOf(Color(0xFF1A3A5C), Color(0xFF388E3C), Color(0xFF4E342E))
                                            )
                                        }
                                    )
                                    .border(1.5.dp, Color(0xFF555555), RoundedCornerShape(2.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (world.gameMode == GameMode.HARDCORE) "💀" else if (world.isFlat) "🟩" else "🌲",
                                    fontSize = 24.sp
                                )

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0x55000000)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.PlayArrow,
                                            contentDescription = "Play",
                                            tint = Color.White,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                            }

                            // World Text Info
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = world.name,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )

                                Text(
                                    text = "${world.name} ($worldIndex) (${dateFormat.format(Date(world.lastPlayed))})",
                                    color = Color(0xFFAAAAAA),
                                    fontSize = 11.5.sp,
                                    fontFamily = FontFamily.Monospace
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    if (world.gameMode == GameMode.HARDCORE) {
                                        Text(
                                            text = "Hardcore Mode",
                                            color = Color(0xFFFF5555),
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(text = ", ", color = Color(0xFFAAAAAA), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                        Text(
                                            text = "Experimental",
                                            color = Color(0xFFFFFF55),
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(text = ", Version: ${world.version}", color = Color(0xFFAAAAAA), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                    } else {
                                        Text(
                                            text = "${world.gameMode.displayName} Mode, Commands, Version: ${world.version}",
                                            color = Color(0xFFAAAAAA),
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
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 1 of Bottom Action Buttons (Screenshot MJLaunch):
        // [Play Selected World] [Create New World]
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            MinecraftMenuButton(
                text = "Play Selected World",
                modifier = Modifier.weight(1f),
                onClick = {
                    selectedWorld?.let(onSelectWorld)
                }
            )
            MinecraftMenuButton(
                text = "Create New World",
                modifier = Modifier.weight(1f),
                onClick = onCreateNew
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Row 2 of Bottom Action Buttons (Screenshot MJLaunch):
        // [Edit] [Delete] [Re-Create] [Cancel]
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            MinecraftMenuButton(
                text = "Edit",
                modifier = Modifier.weight(1f),
                onClick = {
                    selectedWorld?.let {
                        renameText = it.name
                        showRenameDialog = true
                    }
                }
            )
            MinecraftMenuButton(
                text = "Delete",
                modifier = Modifier.weight(1f),
                onClick = {
                    if (selectedWorld != null && worlds.size > 1) {
                        showDeleteConfirmDialog = true
                    }
                }
            )
            MinecraftMenuButton(
                text = "Re-Create",
                modifier = Modifier.weight(1f),
                onClick = onCreateNew
            )
            MinecraftMenuButton(
                text = "Cancel",
                modifier = Modifier.weight(1f),
                onClick = onBack
            )
        }
    }

    // Rename Dialog
    if (showRenameDialog && selectedWorld != null) {
        BasicAlertDialog(onDismissRequest = { showRenameDialog = false }) {
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Edit World Name",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    OutlinedTextField(
                        value = renameText,
                        onValueChange = { renameText = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color(0xFF777777),
                            focusedContainerColor = Color.Black,
                            unfocusedContainerColor = Color.Black
                        )
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MinecraftMenuButton(
                            text = "Save",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                showRenameDialog = false
                            }
                        )
                        MinecraftMenuButton(
                            text = "Cancel",
                            modifier = Modifier.weight(1f),
                            onClick = { showRenameDialog = false }
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog && selectedWorld != null) {
        BasicAlertDialog(onDismissRequest = { showDeleteConfirmDialog = false }) {
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Delete World?",
                        color = Color(0xFFFF5555),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "'${selectedWorld.name}' will be lost forever! (A long time!)",
                        color = Color.White,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Monospace
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MinecraftMenuButton(
                            text = "Delete",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onDelete(selectedWorld.id)
                                showDeleteConfirmDialog = false
                            }
                        )
                        MinecraftMenuButton(
                            text = "Cancel",
                            modifier = Modifier.weight(1f),
                            onClick = { showDeleteConfirmDialog = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AboutScreen(
    versionManager: VersionManager,
    onBack: () -> Unit
) {
    val ver = versionManager.currentVersion

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .padding(20.dp)
    ) {
        Text(
            text = "About Minecraft",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFF444444), RoundedCornerShape(4.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Minecraft Java Edition 26.2", color = Color(0xFF81C784), fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("Release Status: Official Release", color = Color(0xFFFFD54F), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                Text("Java Runtime: ${ver.javaRuntime}", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                Text("Graphics Engine: OpenGL ES 2.0 Shader Pipeline", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                Text("UI Engine: Jetpack Compose Material 3", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                Text("APK Package: com.aistudio.minecraft.jv262", color = Color(0xFF90CAF9), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                Text("Features: Crafter, Mace, Wind Charge, Copper Block, Tuff, Authentic Textures & Sounds", color = Color(0xFFE0E0E0), fontFamily = FontFamily.Monospace, fontSize = 10.sp)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        MinecraftMenuButton(text = "Back", onClick = onBack)
    }
}

@Composable
fun OptionsScreen(
    soundManager: SoundManager,
    fov: Float,
    onFovChange: (Float) -> Unit,
    sensitivity: Float,
    onSensitivityChange: (Float) -> Unit,
    onBack: () -> Unit
) {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        com.example.game.ui.options.GameOptionsState.fov = fov
        com.example.game.ui.options.GameOptionsState.lookSensitivity = sensitivity
    }

    com.example.game.ui.options.JavaOptionsScreen(
        soundManager = soundManager,
        onBack = {
            onFovChange(com.example.game.ui.options.GameOptionsState.fov)
            onSensitivityChange(com.example.game.ui.options.GameOptionsState.lookSensitivity)
            onBack()
        }
    )
}

@Composable
fun MinecraftMenuButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 32.dp,
    fontSize: TextUnit = 12.5.sp
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF555555)),
        shape = RoundedCornerShape(2.dp),
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .border(1.5.dp, Color(0xFF999999), RoundedCornerShape(2.dp))
    ) {
        Text(
            text = text,
            color = Color.White,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

// Dialogs: Realms, Skin, Language, Accessibility, Keyboard
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RealmsDialog(onDismiss: () -> Unit) {
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Minecraft Realms", color = Color(0xFFFFD54F), fontFamily = FontFamily.Monospace, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Realms is a safe, simple way to enjoy an online Minecraft world with friends.", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp, textAlign = TextAlign.Center)
                MinecraftMenuButton(text = "Back", onClick = onDismiss)
            }
        }
    }
}

data class PartyFriend(
    val name: String,
    val avatar: String,
    val initialStatus: String,
    var isInvited: Boolean = false,
    var hasJoined: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreatePartyDialog(
    soundManager: SoundManager,
    onDismiss: () -> Unit
) {
    var partyPrivacy by remember { mutableStateOf("Invite only") } // "Open" or "Invite only"
    val partyMembers = remember { mutableStateListOf<String>("You (Leader)") }
    var partyBannerMessage by remember { mutableStateOf<String?>(null) }
    var addFriendName by remember { mutableStateOf("") }

    val friendsList = remember {
        mutableStateListOf(
            PartyFriend("MasterGreataxe", "🛡️", "Playing in Survival Mode"),
            PartyFriend("NewSasquatch", "🦧", "In the Minecraft Menus"),
            PartyFriend("PitBear", "🐻", "Playing in Creative Mode"),
            PartyFriend("DoctorHoot", "🦉", "Online")
        )
    }

    // Auto-clear notification banner
    LaunchedEffect(partyBannerMessage) {
        if (partyBannerMessage != null) {
            delay(2400)
            partyBannerMessage = null
        }
    }

    // Simulated background player join when party is Open
    LaunchedEffect(partyPrivacy) {
        if (partyPrivacy == "Open") {
            delay(3000)
            val candidate = friendsList.firstOrNull { !it.hasJoined }
            if (candidate != null && partyMembers.size < 15) {
                candidate.hasJoined = true
                partyMembers.add(candidate.name)
                soundManager.playClick()
                partyBannerMessage = "${candidate.name} joined the party! (${partyMembers.size}/15)"
            }
        }
    }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(620.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xF0121212))
                .border(2.dp, Color(0xFF555555), RoundedCornerShape(6.dp))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header (Matching Screenshot [CREATE PARTY (BETA)])
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "[CREATE PARTY (BETA)]",
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Party count badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF2E7D32))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Party (${partyMembers.size}/15)",
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Notification Banner when player joins
                AnimatedVisibility(visible = partyBannerMessage != null) {
                    partyBannerMessage?.let { msg ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF1B5E20))
                                .border(1.dp, Color(0xFF4CAF50), RoundedCornerShape(4.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✔ $msg",
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Two Columns: Left (Party Settings) & Right (Friends List)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // LEFT COLUMN: PARTY SETTINGS (Matching Screenshot)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x661E1E1E))
                            .border(1.dp, Color(0xFF333333), RoundedCornerShape(4.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "PARTY SETTINGS",
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "A party can have up to 15 players.",
                            color = Color(0xFFAAAAAA),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.5.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Party privacy",
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        // Privacy Tabs: [ Open ] | [ Invite only ]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF111111)),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Open Tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (partyPrivacy == "Open") Color(0xFF2E7D32) else Color(0xFF222222))
                                    .clickable {
                                        partyPrivacy = "Open"
                                        soundManager.playClick()
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Open",
                                    color = if (partyPrivacy == "Open") Color.White else Color(0xFFAAAAAA),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Invite only Tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (partyPrivacy == "Invite only") Color(0xFF2E7D32) else Color(0xFF222222))
                                    .clickable {
                                        partyPrivacy = "Invite only"
                                        soundManager.playClick()
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Invite only",
                                    color = if (partyPrivacy == "Invite only") Color.White else Color(0xFFAAAAAA),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = if (partyPrivacy == "Invite only") "Players must be invited to join party."
                            else "Any friend can join your party freely.",
                            color = Color(0xFF888888),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // Current Party Members summary
                        Text(
                            text = "Members in Party:",
                            color = Color(0xFFFFD54F),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(partyMembers) { member ->
                                Text(
                                    text = "• $member",
                                    color = Color(0xFF81C784),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    // RIGHT COLUMN: FRIENDS LIST (Matching Screenshot)
                    Column(
                        modifier = Modifier
                            .weight(1.3f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x661E1E1E))
                            .border(1.dp, Color(0xFF333333), RoundedCornerShape(4.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF2E7D32))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Friends (${friendsList.size})",
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Quick test button to simulate player joining
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0xFF333333))
                                    .clickable {
                                        val nextFriend = friendsList.firstOrNull { !it.hasJoined }
                                        if (nextFriend != null && partyMembers.size < 15) {
                                            nextFriend.hasJoined = true
                                            partyMembers.add(nextFriend.name)
                                            soundManager.playClick()
                                            partyBannerMessage = "${nextFriend.name} joined the party! (${partyMembers.size}/15)"
                                        }
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "+ Join",
                                    color = Color(0xFFFFD54F),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.5.sp
                                )
                            }
                        }

                        // Friends List Items
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(friendsList) { friend ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color(0xFF252525))
                                        .border(
                                            1.dp,
                                            if (friend.hasJoined) Color(0xFF4CAF50) else Color(0xFF3E3E3E),
                                            RoundedCornerShape(3.dp)
                                        )
                                        .padding(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(text = friend.avatar, fontSize = 20.sp)
                                            Column {
                                                Text(
                                                    text = friend.name,
                                                    color = Color.White,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = if (friend.hasJoined) "In Party" else friend.initialStatus,
                                                    color = if (friend.hasJoined) Color(0xFF81C784) else Color(0xFF888888),
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 9.5.sp
                                                )
                                            }
                                        }

                                        // Invite / Joined Button
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(
                                                    when {
                                                        friend.hasJoined -> Color(0xFF1B5E20)
                                                        friend.isInvited -> Color(0xFFE65100)
                                                        else -> Color(0xFF388E3C)
                                                    }
                                                )
                                                .clickable {
                                                    if (!friend.hasJoined) {
                                                        soundManager.playClick()
                                                        friend.isInvited = true
                                                        friend.hasJoined = true
                                                        if (!partyMembers.contains(friend.name)) {
                                                            partyMembers.add(friend.name)
                                                        }
                                                        partyBannerMessage = "${friend.name} joined the party! (${partyMembers.size}/15)"
                                                    }
                                                }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = when {
                                                    friend.hasJoined -> "✔ In Party"
                                                    friend.isInvited -> "Invited"
                                                    else -> "+ Invite"
                                                },
                                                color = Color.White,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Add Gamertag row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = addFriendName,
                                onValueChange = { addFriendName = it },
                                placeholder = { Text("Gamertag...", color = Color.Gray, fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF4CAF50),
                                    unfocusedBorderColor = Color(0xFF555555),
                                    focusedContainerColor = Color.Black,
                                    unfocusedContainerColor = Color.Black
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            )

                            Box(
                                modifier = Modifier
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF2E7D32))
                                    .clickable {
                                        if (addFriendName.isNotBlank()) {
                                            friendsList.add(PartyFriend(addFriendName.trim(), "👤", "Added Friend"))
                                            soundManager.playClick()
                                            addFriendName = ""
                                        }
                                    }
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Add", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Close Button
                Box(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    MinecraftMenuButton(
                        text = "Close",
                        onClick = onDismiss,
                        modifier = Modifier.width(200.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageDialog(onDismiss: () -> Unit) {
    var currentLang by remember { mutableStateOf("English (US)") }

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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Language", color = Color(0xFFFFD54F), fontFamily = FontFamily.Monospace, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                listOf("English (US)", "English (UK)", "Español", "Français", "Deutsch", "日本語").forEach { lang ->
                    MinecraftMenuButton(
                        text = if (currentLang == lang) "✔ $lang" else lang,
                        onClick = { currentLang = lang }
                    )
                }
                MinecraftMenuButton(text = "Done", onClick = onDismiss)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccessibilityDialog(onDismiss: () -> Unit) {
    var highContrast by remember { mutableStateOf(false) }
    var subtitles by remember { mutableStateOf(true) }

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
                Text("Accessibility Settings", color = Color(0xFFFFD54F), fontFamily = FontFamily.Monospace, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("High Contrast", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                    Switch(checked = highContrast, onCheckedChange = { highContrast = it })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Show Subtitles", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                    Switch(checked = subtitles, onCheckedChange = { subtitles = it })
                }

                MinecraftMenuButton(text = "Done", onClick = onDismiss)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KeyboardDialog(onDismiss: () -> Unit) {
    var inputText by remember { mutableStateOf("") }

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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("On-Screen Input", color = Color(0xFFFFD54F), fontFamily = FontFamily.Monospace, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Type commands or messages...", color = Color.Gray, fontFamily = FontFamily.Monospace) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF4CAF50),
                        unfocusedBorderColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                MinecraftMenuButton(text = "Send / Close", onClick = onDismiss)
            }
        }
    }
}
