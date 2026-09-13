package com.example.game.ui.options

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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

enum class OptionsSubScreen {
    HUB,
    ONLINE,
    SKIN,
    MUSIC_SOUNDS,
    VIDEO,
    CONTROLS,
    ACCESSIBILITY,
    LANGUAGE,
    CHAT,
    RESOURCE_PACKS,
    CREDITS,
    TELEMETRY
}

@Composable
fun JavaOptionsScreen(
    soundManager: SoundManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentSubScreen by remember { mutableStateOf(OptionsSubScreen.HUB) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    fun playClick() {
        soundManager.playClick()
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (currentSubScreen) {
            OptionsSubScreen.HUB -> {
                JavaOptionsHubScreen(
                    onNavigate = {
                        playClick()
                        currentSubScreen = it
                    },
                    onDone = {
                        playClick()
                        onBack()
                    }
                )
            }

            OptionsSubScreen.ONLINE -> {
                OnlineOptionsSubScreen(
                    onBack = {
                        playClick()
                        currentSubScreen = OptionsSubScreen.HUB
                    },
                    onShowToast = { toastMessage = it }
                )
            }

            OptionsSubScreen.SKIN -> {
                SkinCustomizationSubScreen(
                    onBack = {
                        playClick()
                        currentSubScreen = OptionsSubScreen.HUB
                    }
                )
            }

            OptionsSubScreen.MUSIC_SOUNDS -> {
                MusicAndSoundSubScreen(
                    soundManager = soundManager,
                    onBack = {
                        playClick()
                        currentSubScreen = OptionsSubScreen.HUB
                    }
                )
            }

            OptionsSubScreen.VIDEO -> {
                VideoSettingsSubScreen(
                    onBack = {
                        playClick()
                        currentSubScreen = OptionsSubScreen.HUB
                    }
                )
            }

            OptionsSubScreen.CONTROLS -> {
                ControlsSubScreen(
                    onBack = {
                        playClick()
                        currentSubScreen = OptionsSubScreen.HUB
                    },
                    onShowToast = { toastMessage = it }
                )
            }

            OptionsSubScreen.ACCESSIBILITY -> {
                AccessibilitySubScreen(
                    onBack = {
                        playClick()
                        currentSubScreen = OptionsSubScreen.HUB
                    },
                    onShowToast = { toastMessage = it }
                )
            }

            OptionsSubScreen.LANGUAGE -> {
                LanguageSubScreen(
                    onBack = {
                        playClick()
                        currentSubScreen = OptionsSubScreen.HUB
                    },
                    onShowToast = { toastMessage = it }
                )
            }

            OptionsSubScreen.CHAT -> {
                ChatSettingsSubScreen(
                    onBack = {
                        playClick()
                        currentSubScreen = OptionsSubScreen.HUB
                    }
                )
            }

            OptionsSubScreen.RESOURCE_PACKS -> {
                ResourcePacksSubScreen(
                    onBack = {
                        playClick()
                        currentSubScreen = OptionsSubScreen.HUB
                    },
                    onShowToast = { toastMessage = it }
                )
            }

            OptionsSubScreen.CREDITS -> {
                CreditsAndAttributionSubScreen(
                    onBack = {
                        playClick()
                        currentSubScreen = OptionsSubScreen.HUB
                    }
                )
            }

            OptionsSubScreen.TELEMETRY -> {
                TelemetryDataSubScreen(
                    onBack = {
                        playClick()
                        currentSubScreen = OptionsSubScreen.HUB
                    },
                    onShowToast = { toastMessage = it }
                )
            }
        }

        // Notification toast banner
        if (toastMessage != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 54.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xEE000000))
                    .border(1.dp, Color(0xFFFFD54F), RoundedCornerShape(3.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = toastMessage ?: "",
                    color = Color(0xFFFFD54F),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// 1. MAIN OPTIONS HUB SCREEN
@Composable
private fun JavaOptionsHubScreen(
    onNavigate: (OptionsSubScreen) -> Unit,
    onDone: () -> Unit
) {
    JavaOptionsScreenScaffold(
        title = "Options",
        onDone = onDone
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Row 1: FOV Slider & Online Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val fovLabel = when (val fovInt = GameOptionsState.fov.toInt()) {
                    70 -> "FOV: Normal"
                    110 -> "FOV: Quake Pro"
                    else -> "FOV: $fovInt°"
                }
                val fovProgress = ((GameOptionsState.fov - 30f) / 80f).coerceIn(0f, 1f)

                JavaOptionSlider(
                    label = fovLabel,
                    value = fovProgress,
                    onValueChange = { progress ->
                        GameOptionsState.fov = 30f + progress * 80f
                    },
                    modifier = Modifier.weight(1f)
                )

                JavaOptionButton(
                    text = "Online...",
                    onClick = { onNavigate(OptionsSubScreen.ONLINE) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 2: Skin Customization... & Music & Sounds...
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Skin Customization...",
                    onClick = { onNavigate(OptionsSubScreen.SKIN) },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Music & Sounds...",
                    onClick = { onNavigate(OptionsSubScreen.MUSIC_SOUNDS) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 3: Video Settings... & Controls...
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Video Settings...",
                    onClick = { onNavigate(OptionsSubScreen.VIDEO) },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Controls...",
                    onClick = { onNavigate(OptionsSubScreen.CONTROLS) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 4: Language... & Chat Settings...
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Language...",
                    onClick = { onNavigate(OptionsSubScreen.LANGUAGE) },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Chat Settings...",
                    onClick = { onNavigate(OptionsSubScreen.CHAT) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 5: Resource Packs... & Accessibility Settings...
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Resource Packs...",
                    onClick = { onNavigate(OptionsSubScreen.RESOURCE_PACKS) },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Accessibility Settings...",
                    onClick = { onNavigate(OptionsSubScreen.ACCESSIBILITY) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 6: Telemetry Data... & Credits & Attribution...
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Telemetry Data...",
                    onClick = { onNavigate(OptionsSubScreen.TELEMETRY) },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Credits & Attribution...",
                    onClick = { onNavigate(OptionsSubScreen.CREDITS) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// 2. ONLINE OPTIONS SCREEN
@Composable
private fun OnlineOptionsSubScreen(
    onBack: () -> Unit,
    onShowToast: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    JavaOptionsScreenScaffold(
        title = "Online Options",
        onDone = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            JavaSectionHeader(title = "Friends List")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Friends List: ${if (GameOptionsState.friendsList) "ON" else "OFF"}",
                    onClick = { GameOptionsState.friendsList = !GameOptionsState.friendsList },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Allow Requests: ${if (GameOptionsState.allowRequests) "ON" else "OFF"}",
                    onClick = { GameOptionsState.allowRequests = !GameOptionsState.allowRequests },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "In-Game Notification: ${if (GameOptionsState.inGameNotification) "ON" else "OFF"}",
                    onClick = { GameOptionsState.inGameNotification = !GameOptionsState.inGameNotification },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Visibility: ${GameOptionsState.onlineVisibility}",
                    onClick = {
                        GameOptionsState.onlineVisibility = when (GameOptionsState.onlineVisibility) {
                            "Full" -> "Friends Only"
                            "Friends Only" -> "Hidden"
                            else -> "Full"
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            JavaOptionButton(
                text = "Xbox Settings...",
                onClick = { onShowToast("Xbox Live services connected.") },
                modifier = Modifier.fillMaxWidth(0.98f)
            )

            JavaSectionHeader(title = "Servers")

            JavaOptionButton(
                text = "Allow Server Listings: ${if (GameOptionsState.allowServerListings) "ON" else "OFF"}",
                onClick = { GameOptionsState.allowServerListings = !GameOptionsState.allowServerListings },
                modifier = Modifier.fillMaxWidth(0.98f)
            )

            JavaSectionHeader(title = "Realms")

            JavaOptionButton(
                text = "Realms Notifications: ${if (GameOptionsState.realmsNotification) "ON" else "OFF"}",
                onClick = { GameOptionsState.realmsNotification = !GameOptionsState.realmsNotification },
                modifier = Modifier.fillMaxWidth(0.98f)
            )
        }
    }
}

// 3. SKIN CUSTOMIZATION SCREEN
@Composable
private fun SkinCustomizationSubScreen(
    onBack: () -> Unit
) {
    JavaOptionsScreenScaffold(
        title = "Skin Customization",
        onDone = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Cape: ${if (GameOptionsState.cape) "ON" else "OFF"}",
                    onClick = { GameOptionsState.cape = !GameOptionsState.cape },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Jacket: ${if (GameOptionsState.jacket) "ON" else "OFF"}",
                    onClick = { GameOptionsState.jacket = !GameOptionsState.jacket },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Left Sleeve: ${if (GameOptionsState.leftSleeve) "ON" else "OFF"}",
                    onClick = { GameOptionsState.leftSleeve = !GameOptionsState.leftSleeve },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Right Sleeve: ${if (GameOptionsState.rightSleeve) "ON" else "OFF"}",
                    onClick = { GameOptionsState.rightSleeve = !GameOptionsState.rightSleeve },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Left Pant Leg: ${if (GameOptionsState.leftPantLeg) "ON" else "OFF"}",
                    onClick = { GameOptionsState.leftPantLeg = !GameOptionsState.leftPantLeg },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Right Pant Leg: ${if (GameOptionsState.rightPantLeg) "ON" else "OFF"}",
                    onClick = { GameOptionsState.rightPantLeg = !GameOptionsState.rightPantLeg },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Hat: ${if (GameOptionsState.hat) "ON" else "OFF"}",
                    onClick = { GameOptionsState.hat = !GameOptionsState.hat },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Main Hand: ${GameOptionsState.mainHand}",
                    onClick = {
                        GameOptionsState.mainHand = if (GameOptionsState.mainHand == "Right") "Left" else "Right"
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// 4. MUSIC & SOUND OPTIONS SCREEN
@Composable
private fun MusicAndSoundSubScreen(
    soundManager: SoundManager,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    JavaOptionsScreenScaffold(
        title = "Music & Sound Options",
        onDone = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Master Volume
            JavaOptionSlider(
                label = "Master Volume: ${(GameOptionsState.masterVolume * 100).toInt()}%",
                value = GameOptionsState.masterVolume,
                onValueChange = {
                    GameOptionsState.masterVolume = it
                    soundManager.isSoundEnabled = it > 0.05f
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Music & Jukebox
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionSlider(
                    label = "Music: ${(GameOptionsState.musicVolume * 100).toInt()}%",
                    value = GameOptionsState.musicVolume,
                    onValueChange = { GameOptionsState.musicVolume = it },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionSlider(
                    label = "Jukebox/Note Blocks: ${(GameOptionsState.jukeboxVolume * 100).toInt()}%",
                    value = GameOptionsState.jukeboxVolume,
                    onValueChange = { GameOptionsState.jukeboxVolume = it },
                    modifier = Modifier.weight(1f)
                )
            }

            // Weather & Blocks
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionSlider(
                    label = "Weather: ${(GameOptionsState.weatherVolume * 100).toInt()}%",
                    value = GameOptionsState.weatherVolume,
                    onValueChange = { GameOptionsState.weatherVolume = it },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionSlider(
                    label = "Blocks: ${(GameOptionsState.blocksVolume * 100).toInt()}%",
                    value = GameOptionsState.blocksVolume,
                    onValueChange = { GameOptionsState.blocksVolume = it },
                    modifier = Modifier.weight(1f)
                )
            }

            // Hostile Mobs & Friendly Mobs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionSlider(
                    label = "Hostile Mobs: ${(GameOptionsState.hostileMobsVolume * 100).toInt()}%",
                    value = GameOptionsState.hostileMobsVolume,
                    onValueChange = { GameOptionsState.hostileMobsVolume = it },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionSlider(
                    label = "Friendly Mobs: ${(GameOptionsState.friendlyMobsVolume * 100).toInt()}%",
                    value = GameOptionsState.friendlyMobsVolume,
                    onValueChange = { GameOptionsState.friendlyMobsVolume = it },
                    modifier = Modifier.weight(1f)
                )
            }

            // Players & Ambient/Environment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionSlider(
                    label = "Players: ${(GameOptionsState.playersVolume * 100).toInt()}%",
                    value = GameOptionsState.playersVolume,
                    onValueChange = { GameOptionsState.playersVolume = it },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionSlider(
                    label = "Ambient/Environment: ${(GameOptionsState.ambientVolume * 100).toInt()}%",
                    value = GameOptionsState.ambientVolume,
                    onValueChange = { GameOptionsState.ambientVolume = it },
                    modifier = Modifier.weight(1f)
                )
            }

            // Voice & UI
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionSlider(
                    label = "Narrator/Voice: ${(GameOptionsState.narratorVolume * 100).toInt()}%",
                    value = GameOptionsState.narratorVolume,
                    onValueChange = { GameOptionsState.narratorVolume = it },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionSlider(
                    label = "UI: ${(GameOptionsState.uiVolume * 100).toInt()}%",
                    value = GameOptionsState.uiVolume,
                    onValueChange = { GameOptionsState.uiVolume = it },
                    modifier = Modifier.weight(1f)
                )
            }

            // Output Device Button
            JavaOptionButton(
                text = "Device: ${GameOptionsState.audioDevice}",
                onClick = {
                    GameOptionsState.audioDevice = if (GameOptionsState.audioDevice == "System Default") "Headphones (OpenSL)" else "System Default"
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// 5. VIDEO SETTINGS SCREEN
@Composable
private fun VideoSettingsSubScreen(
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    JavaOptionsScreenScaffold(
        title = "Video Settings",
        onDone = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            JavaSectionHeader(title = "Display")

            JavaOptionButton(
                text = "Fullscreen Resolution: ${GameOptionsState.fullscreenResolution}",
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Max Framerate: ${GameOptionsState.maxFramerate}",
                    onClick = {
                        GameOptionsState.maxFramerate = when (GameOptionsState.maxFramerate) {
                            "30 fps" -> "60 fps"
                            "60 fps" -> "90 fps"
                            "90 fps" -> "120 fps"
                            "120 fps" -> "144 fps"
                            "144 fps" -> "Unlimited"
                            else -> "30 fps"
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "VSync: ${if (GameOptionsState.vsync) "ON" else "OFF"}",
                    onClick = { GameOptionsState.vsync = !GameOptionsState.vsync },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Reduce FPS when: ${GameOptionsState.reduceFpsWhen}",
                    onClick = {
                        GameOptionsState.reduceFpsWhen = if (GameOptionsState.reduceFpsWhen == "AFK") "OFF" else "AFK"
                    },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "GUI Scale: ${GameOptionsState.guiScale}",
                    onClick = {
                        GameOptionsState.guiScale = when (GameOptionsState.guiScale) {
                            "Auto" -> "1"
                            "1" -> "2"
                            "2" -> "3"
                            "3" -> "4"
                            else -> "Auto"
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Fullscreen: ${if (GameOptionsState.fullscreen) "ON" else "OFF"}",
                    onClick = { GameOptionsState.fullscreen = !GameOptionsState.fullscreen },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Exclusive Fullscreen: ${if (GameOptionsState.exclusiveFullscreen) "ON" else "OFF"}",
                    onClick = { GameOptionsState.exclusiveFullscreen = !GameOptionsState.exclusiveFullscreen },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Brightness: ${GameOptionsState.brightness}",
                    onClick = {
                        GameOptionsState.brightness = when (GameOptionsState.brightness) {
                            "Moody" -> "25%"
                            "25%" -> "50%"
                            "50%" -> "75%"
                            "75%" -> "Bright"
                            else -> "Moody"
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Graphics API: ${GameOptionsState.graphicsApi}",
                    onClick = {
                        GameOptionsState.graphicsApi = when (GameOptionsState.graphicsApi) {
                            "Default" -> "OpenGL ES 3.2"
                            "OpenGL ES 3.2" -> "Vulkan (Zink)"
                            else -> "Default"
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            JavaSectionHeader(title = "Quality & Performance")

            JavaOptionButton(
                text = "Preset: ${GameOptionsState.preset}",
                onClick = {
                    GameOptionsState.preset = when (GameOptionsState.preset) {
                        "Custom" -> "Fast"
                        "Fast" -> "Fancy"
                        "Fancy" -> "Fabulous!"
                        else -> "Custom"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Biome Blend: ${GameOptionsState.biomeBlend}",
                    onClick = {
                        GameOptionsState.biomeBlend = when (GameOptionsState.biomeBlend) {
                            "OFF" -> "3x3"
                            "3x3" -> "5x5 (Normal)"
                            "5x5 (Normal)" -> "7x7"
                            else -> "OFF"
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionSlider(
                    label = "Render Distance: ${GameOptionsState.renderDistance} Chunks",
                    value = ((GameOptionsState.renderDistance - 4f) / 28f).coerceIn(0f, 1f),
                    onValueChange = { progress ->
                        GameOptionsState.renderDistance = (4 + progress * 28).toInt()
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Chunk Builder: ${GameOptionsState.chunkBuilder}",
                    onClick = {
                        GameOptionsState.chunkBuilder = if (GameOptionsState.chunkBuilder == "Threaded") "Semi-blocking" else "Threaded"
                    },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionSlider(
                    label = "Simulation Distance: ${GameOptionsState.simulationDistance} Chunks",
                    value = ((GameOptionsState.simulationDistance - 4f) / 28f).coerceIn(0f, 1f),
                    onValueChange = { progress ->
                        GameOptionsState.simulationDistance = (4 + progress * 28).toInt()
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Smooth Lighting: ${GameOptionsState.smoothLighting}",
                    onClick = {
                        GameOptionsState.smoothLighting = when (GameOptionsState.smoothLighting) {
                            "OFF" -> "Minimum"
                            "Minimum" -> "Maximum"
                            else -> "OFF"
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Clouds: ${GameOptionsState.clouds}",
                    onClick = {
                        GameOptionsState.clouds = when (GameOptionsState.clouds) {
                            "Fancy" -> "Fast"
                            "Fast" -> "OFF"
                            else -> "Fancy"
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Particles: ${GameOptionsState.particles}",
                    onClick = {
                        GameOptionsState.particles = when (GameOptionsState.particles) {
                            "All" -> "Decreased"
                            "Decreased" -> "Minimal"
                            else -> "All"
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Mipmap Levels: ${GameOptionsState.mipmapLevels}",
                    onClick = {
                        GameOptionsState.mipmapLevels = (GameOptionsState.mipmapLevels % 4) + 1
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Entity Shadows: ${if (GameOptionsState.entityShadows) "ON" else "OFF"}",
                    onClick = { GameOptionsState.entityShadows = !GameOptionsState.entityShadows },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Entity Distance: ${GameOptionsState.entityDistance}",
                    onClick = {
                        GameOptionsState.entityDistance = when (GameOptionsState.entityDistance) {
                            "50%" -> "75%"
                            "75%" -> "100%"
                            "100%" -> "125%"
                            "125%" -> "150%"
                            else -> "50%"
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Menu Background Blur: ${GameOptionsState.menuBackgroundBlur}",
                    onClick = {
                        GameOptionsState.menuBackgroundBlur = (GameOptionsState.menuBackgroundBlur % 5) + 1
                    },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Cloud Distance: ${GameOptionsState.cloudDistance}",
                    onClick = {
                        GameOptionsState.cloudDistance = if (GameOptionsState.cloudDistance == "128 Chunks") "64 Chunks" else "128 Chunks"
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "See-Through Leaves: ${if (GameOptionsState.seeThroughLeaves) "ON" else "OFF"}",
                    onClick = { GameOptionsState.seeThroughLeaves = !GameOptionsState.seeThroughLeaves },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Improved Transparency: ${if (GameOptionsState.improvedTransparency) "ON" else "OFF"}",
                    onClick = { GameOptionsState.improvedTransparency = !GameOptionsState.improvedTransparency },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// 6. CONTROLS SCREEN
@Composable
private fun ControlsSubScreen(
    onBack: () -> Unit,
    onShowToast: (String) -> Unit
) {
    JavaOptionsScreenScaffold(
        title = "Controls",
        onDone = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Mouse Settings...",
                    onClick = { onShowToast("Mouse sensitivity & inverted axis settings configured.") },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Key Binds...",
                    onClick = { onShowToast("Pojav custom controls editor opened in-game.") },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Sneak: ${GameOptionsState.sneakHold}",
                    onClick = {
                        GameOptionsState.sneakHold = if (GameOptionsState.sneakHold == "Hold") "Toggle" else "Hold"
                    },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Sprint: ${GameOptionsState.sprintHold}",
                    onClick = {
                        GameOptionsState.sprintHold = if (GameOptionsState.sprintHold == "Hold") "Toggle" else "Hold"
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Attack/Destroy: ${GameOptionsState.attackDestroyHold}",
                    onClick = {
                        GameOptionsState.attackDestroyHold = if (GameOptionsState.attackDestroyHold == "Hold") "Click" else "Hold"
                    },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Use Item/Place Block: ${GameOptionsState.useItemPlaceBlockHold}",
                    onClick = {
                        GameOptionsState.useItemPlaceBlockHold = if (GameOptionsState.useItemPlaceBlockHold == "Hold") "Click" else "Hold"
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Auto-Jump: ${if (GameOptionsState.autoJump) "ON" else "OFF"}",
                    onClick = { GameOptionsState.autoJump = !GameOptionsState.autoJump },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Sprint Window: ${GameOptionsState.sprintWindow}",
                    onClick = {
                        GameOptionsState.sprintWindow = (GameOptionsState.sprintWindow % 10) + 1
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            JavaOptionButton(
                text = "Operator Items Tab: ${if (GameOptionsState.operatorItemsTab) "ON" else "OFF"}",
                onClick = { GameOptionsState.operatorItemsTab = !GameOptionsState.operatorItemsTab },
                modifier = Modifier.fillMaxWidth(0.98f)
            )
        }
    }
}

// 7. ACCESSIBILITY SETTINGS SCREEN
@Composable
private fun AccessibilitySubScreen(
    onBack: () -> Unit,
    onShowToast: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    JavaOptionsScreenScaffold(
        title = "Accessibility Settings",
        extraBottomButton = {
            JavaOptionButton(
                text = "Accessibility Guide",
                onClick = { onShowToast("Visit help.minecraft.net for accessibility resources.") }
            )
        },
        onDone = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Narrator: ${GameOptionsState.narratorStatus}",
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Controls...",
                    onClick = { onShowToast("Controls mapped.") },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Closed Captions: ${if (GameOptionsState.closedCaptions) "ON" else "OFF"}",
                    onClick = { GameOptionsState.closedCaptions = !GameOptionsState.closedCaptions },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "High Contrast: ${if (GameOptionsState.highContrast) "ON" else "OFF"}",
                    onClick = { GameOptionsState.highContrast = !GameOptionsState.highContrast },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Menu Background Blur: ${GameOptionsState.menuBackgroundBlur}",
                    onClick = { GameOptionsState.menuBackgroundBlur = (GameOptionsState.menuBackgroundBlur % 5) + 1 },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Text Background Opacity: ${GameOptionsState.textBackgroundOpacity}",
                    onClick = {
                        GameOptionsState.textBackgroundOpacity = when (GameOptionsState.textBackgroundOpacity) {
                            "0%" -> "25%"
                            "25%" -> "50%"
                            "50%" -> "75%"
                            "75%" -> "100%"
                            else -> "0%"
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Text Background: ${GameOptionsState.textBackground}",
                    onClick = {
                        GameOptionsState.textBackground = if (GameOptionsState.textBackground == "Chat") "Everywhere" else "Chat"
                    },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Chat Text Opacity: ${GameOptionsState.chatTextOpacity}",
                    onClick = {
                        GameOptionsState.chatTextOpacity = if (GameOptionsState.chatTextOpacity == "100%") "50%" else "100%"
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Line Spacing: ${GameOptionsState.chatLineSpacing}",
                    onClick = {
                        GameOptionsState.chatLineSpacing = if (GameOptionsState.chatLineSpacing == "0%") "10%" else "0%"
                    },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Chat Delay: ${GameOptionsState.chatDelay}",
                    onClick = {
                        GameOptionsState.chatDelay = if (GameOptionsState.chatDelay == "None") "1.0s" else "None"
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Notification Time: ${GameOptionsState.notificationTime}",
                    onClick = {
                        GameOptionsState.notificationTime = if (GameOptionsState.notificationTime == "1.0x") "2.0x" else "1.0x"
                    },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "View Bobbing: ${if (GameOptionsState.viewBobbing) "ON" else "OFF"}",
                    onClick = { GameOptionsState.viewBobbing = !GameOptionsState.viewBobbing },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Distortion Effects: ${GameOptionsState.distortionEffects}",
                    onClick = {
                        GameOptionsState.distortionEffects = if (GameOptionsState.distortionEffects == "100%") "50%" else "100%"
                    },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "FOV Effects: ${GameOptionsState.fovEffects}",
                    onClick = {
                        GameOptionsState.fovEffects = if (GameOptionsState.fovEffects == "100%") "OFF" else "100%"
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Darkness Pulsing: ${GameOptionsState.darknessPulsing}",
                    onClick = {
                        GameOptionsState.darknessPulsing = if (GameOptionsState.darknessPulsing == "100%") "OFF" else "100%"
                    },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Damage Tilt: ${GameOptionsState.damageTilt}",
                    onClick = {
                        GameOptionsState.damageTilt = if (GameOptionsState.damageTilt == "100%") "50%" else "100%"
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Glint Speed: ${GameOptionsState.glintSpeed}",
                    onClick = {
                        GameOptionsState.glintSpeed = if (GameOptionsState.glintSpeed == "50%") "100%" else "50%"
                    },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Glint Strength: ${GameOptionsState.glintStrength}",
                    onClick = {
                        GameOptionsState.glintStrength = if (GameOptionsState.glintStrength == "75%") "100%" else "75%"
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Hide Sky Flashes: ${if (GameOptionsState.hideSkyFlashes) "ON" else "OFF"}",
                    onClick = { GameOptionsState.hideSkyFlashes = !GameOptionsState.hideSkyFlashes },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Monochrome Logo: ${if (GameOptionsState.monochromeLogo) "ON" else "OFF"}",
                    onClick = { GameOptionsState.monochromeLogo = !GameOptionsState.monochromeLogo },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Panorama Scroll Speed: ${GameOptionsState.panoramaScrollSpeed}",
                    onClick = {
                        GameOptionsState.panoramaScrollSpeed = if (GameOptionsState.panoramaScrollSpeed == "100%") "50%" else "100%"
                    },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Hide Splash Texts: ${if (GameOptionsState.hideSplashTexts) "ON" else "OFF"}",
                    onClick = { GameOptionsState.hideSplashTexts = !GameOptionsState.hideSplashTexts },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Narrator Hotkey: ${if (GameOptionsState.narratorHotkey) "ON" else "OFF"}",
                    onClick = { GameOptionsState.narratorHotkey = !GameOptionsState.narratorHotkey },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Rotate with Minecarts: ${if (GameOptionsState.rotateWithMinecarts) "ON" else "OFF"}",
                    onClick = { GameOptionsState.rotateWithMinecarts = !GameOptionsState.rotateWithMinecarts },
                    modifier = Modifier.weight(1f)
                )
            }

            JavaOptionButton(
                text = "High Contrast Block Outlines: ${if (GameOptionsState.highContrastBlockOutlines) "ON" else "OFF"}",
                onClick = { GameOptionsState.highContrastBlockOutlines = !GameOptionsState.highContrastBlockOutlines },
                modifier = Modifier.fillMaxWidth(0.98f)
            )
        }
    }
}

// 8. LANGUAGE SCREEN
@Composable
private fun LanguageSubScreen(
    onBack: () -> Unit,
    onShowToast: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val allLanguages = listOf(
        "English (United Kingdom)",
        "English (New Zealand)",
        "Pirate Speak (The Seven Seas)",
        "ɥs!ןbuƎ (umoᗡ ap!sd∩)",
        "English (US)",
        "Anglish (Oned Riches)",
        "Shakespearean English (Kingdom of England)",
        "Esperanto (Esperantujo)",
        "Español (Argentina)",
        "Deutsch (Deutschland)",
        "Français (France)",
        "日本語 (日本)"
    )

    val filtered = remember(searchQuery) {
        if (searchQuery.isBlank()) allLanguages
        else allLanguages.filter { it.contains(searchQuery, ignoreCase = true) }
    }

    JavaOptionsScreenScaffold(
        title = "Language",
        extraBottomButton = {
            JavaOptionButton(
                text = "Font Settings...",
                onClick = { onShowToast("Force Unicode Font: OFF") }
            )
        },
        onDone = onBack
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "_",
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth(0.95f)
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

            Spacer(modifier = Modifier.height(6.dp))

            // Scrollable Language List
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(0.95f)
                    .background(Color(0x66000000))
                    .border(1.dp, Color(0xFF333333))
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    items(filtered) { lang ->
                        val isSelected = lang == GameOptionsState.selectedLanguage
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isSelected) Color(0x33FFFFFF) else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isSelected) Color.White else Color.Transparent,
                                    RoundedCornerShape(2.dp)
                                )
                                .clickable { GameOptionsState.selectedLanguage = lang }
                                .padding(vertical = 5.dp, horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = lang,
                                color = if (isSelected) Color.White else Color(0xFFCCCCCC),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "(Language translations may not be 100% accurate)",
                color = Color(0xFF888888),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
        }
    }
}

// 9. CHAT SETTINGS SCREEN
@Composable
private fun ChatSettingsSubScreen(
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    JavaOptionsScreenScaffold(
        title = "Chat Settings",
        onDone = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Chat: ${GameOptionsState.chatVisibility}",
                    onClick = {
                        GameOptionsState.chatVisibility = when (GameOptionsState.chatVisibility) {
                            "Shown" -> "Commands Only"
                            "Commands Only" -> "Hidden"
                            else -> "Shown"
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Colors: ${if (GameOptionsState.chatColors) "ON" else "OFF"}",
                    onClick = { GameOptionsState.chatColors = !GameOptionsState.chatColors },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Web Links: ${if (GameOptionsState.webLinks) "ON" else "OFF"}",
                    onClick = { GameOptionsState.webLinks = !GameOptionsState.webLinks },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Prompt on Links: ${if (GameOptionsState.promptOnLinks) "ON" else "OFF"}",
                    onClick = { GameOptionsState.promptOnLinks = !GameOptionsState.promptOnLinks },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Chat Text Opacity: ${GameOptionsState.chatTextOpacity}",
                    onClick = {
                        GameOptionsState.chatTextOpacity = if (GameOptionsState.chatTextOpacity == "100%") "50%" else "100%"
                    },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Text Background Opacity: ${GameOptionsState.textBackgroundOpacity}",
                    onClick = {
                        GameOptionsState.textBackgroundOpacity = when (GameOptionsState.textBackgroundOpacity) {
                            "0%" -> "25%"
                            "25%" -> "50%"
                            "50%" -> "75%"
                            else -> "0%"
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Chat Text Size: ${GameOptionsState.chatTextSize}",
                    onClick = {
                        GameOptionsState.chatTextSize = if (GameOptionsState.chatTextSize == "100%") "120%" else "100%"
                    },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Line Spacing: ${GameOptionsState.chatLineSpacing}",
                    onClick = {
                        GameOptionsState.chatLineSpacing = if (GameOptionsState.chatLineSpacing == "0%") "10%" else "0%"
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Chat Delay: ${GameOptionsState.chatDelay}",
                    onClick = {
                        GameOptionsState.chatDelay = if (GameOptionsState.chatDelay == "None") "1.0s" else "None"
                    },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Width: ${GameOptionsState.chatWidth}",
                    onClick = {
                        GameOptionsState.chatWidth = if (GameOptionsState.chatWidth == "320px") "400px" else "320px"
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Focused Height: ${GameOptionsState.chatFocusedHeight}",
                    onClick = {
                        GameOptionsState.chatFocusedHeight = if (GameOptionsState.chatFocusedHeight == "180px") "220px" else "180px"
                    },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Unfocused Height: ${GameOptionsState.chatUnfocusedHeight}",
                    onClick = {
                        GameOptionsState.chatUnfocusedHeight = if (GameOptionsState.chatUnfocusedHeight == "90px") "120px" else "90px"
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Narrator: ${GameOptionsState.narratorStatus}",
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Command Suggestions: ${if (GameOptionsState.commandSuggestions) "ON" else "OFF"}",
                    onClick = { GameOptionsState.commandSuggestions = !GameOptionsState.commandSuggestions },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Hide Matched Names: ${if (GameOptionsState.hideMatchedNames) "ON" else "OFF"}",
                    onClick = { GameOptionsState.hideMatchedNames = !GameOptionsState.hideMatchedNames },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Reduced Debug Info: ${if (GameOptionsState.reducedDebugInfo) "ON" else "OFF"}",
                    onClick = { GameOptionsState.reducedDebugInfo = !GameOptionsState.reducedDebugInfo },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JavaOptionButton(
                    text = "Only Show Secure Chat: ${if (GameOptionsState.onlyShowSecureChat) "ON" else "OFF"}",
                    onClick = { GameOptionsState.onlyShowSecureChat = !GameOptionsState.onlyShowSecureChat },
                    modifier = Modifier.weight(1f)
                )
                JavaOptionButton(
                    text = "Save Unsent Chats: ${if (GameOptionsState.saveUnsentChats) "ON" else "OFF"}",
                    onClick = { GameOptionsState.saveUnsentChats = !GameOptionsState.saveUnsentChats },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// 10. RESOURCE PACKS SCREEN
@Composable
private fun ResourcePacksSubScreen(
    onBack: () -> Unit,
    onShowToast: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    JavaOptionsScreenScaffold(
        title = "Select Resource Packs",
        extraBottomButton = {
            JavaOptionButton(
                text = "Open Pack Folder",
                onClick = { onShowToast("Packs stored in /sdcard/games/pojavlauncher/resourcepacks") }
            )
        },
        onDone = onBack
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Drag and drop files into this window to add packs",
                color = Color(0xFFAAAAAA),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Search box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "_",
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth(0.98f)
                    .height(42.dp)
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

            Spacer(modifier = Modifier.height(6.dp))

            // 2 Columns: Available and Selected
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(0.98f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // LEFT: Available
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .border(1.5.dp, Color(0xFF333333))
                        .background(Color(0x77000000))
                        .padding(6.dp)
                ) {
                    Text(
                        text = "Available",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(GameOptionsState.availableResourcePacks) { pack ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0xFF222222))
                                    .border(1.dp, Color(0xFF444444))
                                    .clickable {
                                        // Move to selected
                                        GameOptionsState.availableResourcePacks.remove(pack)
                                        GameOptionsState.selectedResourcePacks.add(pack)
                                    }
                                    .padding(6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = pack.iconEmoji, fontSize = 20.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = pack.title,
                                        color = Color.White,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = pack.description,
                                        color = Color(0xFFAAAAAA),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp
                                    )
                                }
                                Text(text = "▶", color = Color(0xFF81C784), fontSize = 12.sp)
                            }
                        }
                    }
                }

                // RIGHT: Selected
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .border(1.5.dp, Color(0xFF333333))
                        .background(Color(0x77000000))
                        .padding(6.dp)
                ) {
                    Text(
                        text = "Selected",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(GameOptionsState.selectedResourcePacks) { pack ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0xFF222222))
                                    .border(1.dp, Color(0xFF444444))
                                    .clickable {
                                        if (pack.id != "default") {
                                            // Move back to available
                                            GameOptionsState.selectedResourcePacks.remove(pack)
                                            GameOptionsState.availableResourcePacks.add(pack)
                                        }
                                    }
                                    .padding(6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (pack.id != "default") {
                                    Text(text = "◀", color = Color(0xFFFF8A80), fontSize = 12.sp)
                                }
                                Text(text = pack.iconEmoji, fontSize = 20.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = pack.title,
                                        color = Color.White,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = pack.description,
                                        color = Color(0xFFAAAAAA),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 11. CREDITS AND ATTRIBUTION SCREEN
@Composable
private fun CreditsAndAttributionSubScreen(
    onBack: () -> Unit
) {
    var activeSubModal by remember { mutableStateOf<String?>(null) }

    if (activeSubModal == "CreditsVideo") {
        com.example.game.ui.CreditsVideoScreen(
            onBack = { activeSubModal = null }
        )
        return
    }

    JavaOptionsScreenScaffold(
        title = "Credits and Attribution",
        onDone = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .padding(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            JavaOptionButton(
                text = "▶ Watch Credits Video",
                onClick = { activeSubModal = "CreditsVideo" },
                modifier = Modifier.fillMaxWidth()
            )

            JavaOptionButton(
                text = "Credits",
                onClick = { activeSubModal = "Credits" },
                modifier = Modifier.fillMaxWidth()
            )

            JavaOptionButton(
                text = "Attribution",
                onClick = { activeSubModal = "Attribution" },
                modifier = Modifier.fillMaxWidth()
            )

            JavaOptionButton(
                text = "Licenses",
                onClick = { activeSubModal = "Licenses" },
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (activeSubModal != null) {
            CreditsDetailDialog(
                type = activeSubModal ?: "",
                onPlayVideo = { activeSubModal = "CreditsVideo" },
                onDismiss = { activeSubModal = null }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreditsDetailDialog(
    type: String,
    onPlayVideo: () -> Unit = {},
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = type,
                    color = Color(0xFFFFD54F),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = when (type) {
                        "Credits" -> "Minecraft: Java Edition 26.2\n\nOfficial Credits Team:\n• Founder: Sharpness\n• Co-Founder & CCO: Dominator\n• Game Director: Sharpness\n• Head of Studio: Sharpness\n• Lead Dev & Designer: Vortex\n• Gameplay Programmer: Void-X\n• Graphics Designer: Sharpness\n• Narrative Director: Vortex\n• Lead Artist: Void-X\n• User Experience: Dominator\n• Audio Director: Vortex"
                        "Attribution" -> "Music by C418, Lena Raine, Kumi Tanioka.\nSound effects recorded and mixed by Mojang Studios.\nOriginal voxel textures & font by Mojang."
                        else -> "Open Source Software:\n• LWJGL 3 (BSD-3)\n• GLFW (zlib/libpng)\n• Jetpack Compose & AndroidX (Apache 2.0)\n• PojavLauncher Core (GPL-3.0)"
                    },
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )

                if (type == "Credits") {
                    JavaOptionButton(
                        text = "▶ Play Credits Video",
                        onClick = onPlayVideo,
                        modifier = Modifier.fillMaxWidth(0.75f)
                    )
                }

                JavaOptionButton(
                    text = "Done",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(0.6f)
                )
            }
        }
    }
}

// 12. TELEMETRY DATA SCREEN
@Composable
private fun TelemetryDataSubScreen(
    onBack: () -> Unit,
    onShowToast: (String) -> Unit
) {
    JavaOptionsScreenScaffold(
        title = "Telemetry Data",
        onDone = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            JavaOptionButton(
                text = "Data Collection: ${GameOptionsState.telemetryDataCollection}",
                onClick = {
                    GameOptionsState.telemetryDataCollection = if (GameOptionsState.telemetryDataCollection == "Minimal") "All" else "Minimal"
                },
                modifier = Modifier.fillMaxWidth()
            )

            JavaOptionButton(
                text = "Open My Data Folder",
                onClick = { onShowToast("Saved in local application sandbox.") },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "We only collect telemetry required to maintain game stability, frame rates, and crash diagnostics.",
                color = Color(0xFFAAAAAA),
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
