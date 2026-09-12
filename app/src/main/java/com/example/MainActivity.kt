package com.example

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.game.audio.SoundManager
import com.example.game.persistence.WorldMetadata
import com.example.game.persistence.WorldStorage
import com.example.game.ui.GameScreen
import com.example.game.ui.TitleScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private lateinit var soundManager: SoundManager
    private lateinit var storage: WorldStorage
    private lateinit var versionManager: com.example.game.version.VersionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        enableEdgeToEdge()

        // Hide system bars for immersive Minecraft gameplay
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())

        soundManager = SoundManager(this)
        storage = WorldStorage(this)
        versionManager = com.example.game.version.VersionManager(this)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    MinecraftApp(soundManager, storage, versionManager)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}

@Composable
fun MinecraftApp(
    soundManager: SoundManager,
    storage: WorldStorage,
    versionManager: com.example.game.version.VersionManager
) {
    var activeWorld by remember { mutableStateOf<WorldMetadata?>(null) }
    var savedWorlds by remember { mutableStateOf(storage.listWorlds()) }

    var fov by remember { mutableFloatStateOf(75f) }
    var sensitivity by remember { mutableFloatStateOf(1.0f) }

    val currentWorld = activeWorld
    if (currentWorld != null) {
        GameScreen(
            worldMeta = currentWorld,
            storage = storage,
            soundManager = soundManager,
            fov = fov,
            onFovChange = { fov = it },
            sensitivity = sensitivity,
            onSensitivityChange = { sensitivity = it },
            onExitToTitle = {
                savedWorlds = storage.listWorlds()
                activeWorld = null
            }
        )
    } else {
        TitleScreen(
            savedWorlds = savedWorlds,
            soundManager = soundManager,
            versionManager = versionManager,
            fov = fov,
            onFovChange = { fov = it },
            sensitivity = sensitivity,
            onSensitivityChange = { sensitivity = it },
            onPlayWorld = { world ->
                activeWorld = world
            },
            onCreateWorld = { name, seed, mode, isFlat, version ->
                val newWorld = storage.createWorld(name, seed, mode, isFlat, version)
                savedWorlds = storage.listWorlds()
                activeWorld = newWorld
            },
            onDeleteWorld = { id ->
                storage.deleteWorld(id)
                savedWorlds = storage.listWorlds()
            }
        )
    }
}

