package com.example.game.ui.options

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class ResourcePackEntry(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val isBuiltIn: Boolean = true
)

object GameOptionsState {
    // FOV & Sensitivity
    var fov by mutableFloatStateOf(70f)
    var lookSensitivity by mutableFloatStateOf(1.0f)

    // Music & Sounds
    var masterVolume by mutableFloatStateOf(1.0f)
    var musicVolume by mutableFloatStateOf(1.0f)
    var jukeboxVolume by mutableFloatStateOf(1.0f)
    var weatherVolume by mutableFloatStateOf(1.0f)
    var blocksVolume by mutableFloatStateOf(1.0f)
    var hostileMobsVolume by mutableFloatStateOf(1.0f)
    var friendlyMobsVolume by mutableFloatStateOf(1.0f)
    var playersVolume by mutableFloatStateOf(1.0f)
    var ambientVolume by mutableFloatStateOf(1.0f)
    var narratorVolume by mutableFloatStateOf(1.0f)
    var uiVolume by mutableFloatStateOf(1.0f)
    var audioDevice by mutableStateOf("System Default")

    // Skin Customization
    var cape by mutableStateOf(true)
    var jacket by mutableStateOf(true)
    var leftSleeve by mutableStateOf(true)
    var rightSleeve by mutableStateOf(true)
    var leftPantLeg by mutableStateOf(true)
    var rightPantLeg by mutableStateOf(true)
    var hat by mutableStateOf(true)
    var mainHand by mutableStateOf("Right") // "Right" or "Left"

    // Video Settings - Display
    var fullscreenResolution by mutableStateOf("Current")
    var maxFramerate by mutableStateOf("120 fps")
    var vsync by mutableStateOf(true)
    var reduceFpsWhen by mutableStateOf("AFK")
    var guiScale by mutableStateOf("3")
    var fullscreen by mutableStateOf(false)
    var exclusiveFullscreen by mutableStateOf(false)
    var brightness by mutableStateOf("Moody")
    var graphicsApi by mutableStateOf("Default")

    // Video Settings - Quality & Performance
    var preset by mutableStateOf("Custom")
    var biomeBlend by mutableStateOf("5x5 (Normal)")
    var renderDistance by mutableIntStateOf(12)
    var chunkBuilder by mutableStateOf("Threaded")
    var simulationDistance by mutableIntStateOf(12)
    var smoothLighting by mutableStateOf("OFF")
    var clouds by mutableStateOf("Fancy")
    var particles by mutableStateOf("All")
    var mipmapLevels by mutableIntStateOf(4)
    var entityShadows by mutableStateOf(true)
    var entityDistance by mutableStateOf("100%")
    var menuBackgroundBlur by mutableIntStateOf(5)
    var cloudDistance by mutableStateOf("128 Chunks")
    var seeThroughLeaves by mutableStateOf(true)
    var improvedTransparency by mutableStateOf(false)

    // Controls
    var sneakHold by mutableStateOf("Hold") // "Hold" or "Toggle"
    var sprintHold by mutableStateOf("Hold") // "Hold" or "Toggle"
    var attackDestroyHold by mutableStateOf("Hold")
    var useItemPlaceBlockHold by mutableStateOf("Hold")
    var autoJump by mutableStateOf(false)
    var sprintWindow by mutableIntStateOf(7)
    var operatorItemsTab by mutableStateOf(false)

    // Online Options
    var friendsList by mutableStateOf(false)
    var allowRequests by mutableStateOf(false)
    var inGameNotification by mutableStateOf(false)
    var onlineVisibility by mutableStateOf("Full")
    var allowServerListings by mutableStateOf(true)
    var realmsNotification by mutableStateOf(true)

    // Chat Settings
    var chatVisibility by mutableStateOf("Shown") // "Shown", "Commands Only", "Hidden"
    var chatColors by mutableStateOf(true)
    var webLinks by mutableStateOf(true)
    var promptOnLinks by mutableStateOf(true)
    var chatTextOpacity by mutableStateOf("100%")
    var textBackgroundOpacity by mutableStateOf("50%")
    var chatTextSize by mutableStateOf("100%")
    var chatLineSpacing by mutableStateOf("0%")
    var chatDelay by mutableStateOf("None")
    var chatWidth by mutableStateOf("320px")
    var chatFocusedHeight by mutableStateOf("180px")
    var chatUnfocusedHeight by mutableStateOf("90px")
    var narratorStatus by mutableStateOf("Not Available")
    var commandSuggestions by mutableStateOf(true)
    var hideMatchedNames by mutableStateOf(true)
    var reducedDebugInfo by mutableStateOf(false)
    var onlyShowSecureChat by mutableStateOf(false)
    var saveUnsentChats by mutableStateOf(false)

    // Accessibility Settings
    var closedCaptions by mutableStateOf(false)
    var highContrast by mutableStateOf(false)
    var textBackground by mutableStateOf("Chat")
    var notificationTime by mutableStateOf("1.0x")
    var viewBobbing by mutableStateOf(false)
    var distortionEffects by mutableStateOf("100%")
    var fovEffects by mutableStateOf("100%")
    var darknessPulsing by mutableStateOf("100%")
    var damageTilt by mutableStateOf("100%")
    var glintSpeed by mutableStateOf("50%")
    var glintStrength by mutableStateOf("75%")
    var hideSkyFlashes by mutableStateOf(false)
    var monochromeLogo by mutableStateOf(false)
    var panoramaScrollSpeed by mutableStateOf("100%")
    var hideSplashTexts by mutableStateOf(false)
    var narratorHotkey by mutableStateOf(true)
    var rotateWithMinecarts by mutableStateOf(false)
    var highContrastBlockOutlines by mutableStateOf(false)

    // Language
    var selectedLanguage by mutableStateOf("English (US)")

    // Resource Packs
    val availableResourcePacks = mutableStateListOf(
        ResourcePackEntry(
            id = "high_contrast",
            title = "High Contrast",
            description = "Enhances the UI contrast of Minecraft (built-in)",
            iconEmoji = "🟦"
        ),
        ResourcePackEntry(
            id = "programmer_art",
            title = "Programmer Art",
            description = "The classic look of Minecraft (built-in)",
            iconEmoji = "🧱"
        )
    )

    val selectedResourcePacks = mutableStateListOf(
        ResourcePackEntry(
            id = "default",
            title = "Default",
            description = "The default look and feel of Minecraft (built-in)",
            iconEmoji = "🌱"
        )
    )

    // Telemetry Data
    var telemetryDataCollection by mutableStateOf("Minimal")
}
