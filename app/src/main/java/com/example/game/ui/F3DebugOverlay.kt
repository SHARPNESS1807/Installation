package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.entities.Player
import com.example.game.world.RaycastResult
import com.example.game.world.World
import java.util.Locale
import kotlin.math.floor

@Composable
fun F3DebugOverlay(
    player: Player,
    world: World,
    raycast: RaycastResult,
    modifier: Modifier = Modifier,
    versionName: String = "26.2",
    javaRuntime: String = "OpenJDK 21.0.5 64-Bit (Official Runtime)"
) {
    val yawNorm = ((player.yaw % 360f) + 360f) % 360f
    val facing = when {
        yawNorm in 45f..135f -> "west (Towards negative X)"
        yawNorm in 135f..225f -> "north (Towards negative Z)"
        yawNorm in 225f..315f -> "east (Towards positive X)"
        else -> "south (Towards positive Z)"
    }

    val blockX = floor(player.x).toInt()
    val blockY = floor(player.y).toInt()
    val blockZ = floor(player.z).toInt()

    val chunkX = floor(blockX.toDouble() / World.CHUNK_SIZE).toInt()
    val chunkZ = floor(blockZ.toDouble() / World.CHUNK_SIZE).toInt()

    val biome = world.getBiomeAt(blockX, blockZ).id

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 28.dp, start = 8.dp, end = 8.dp)
    ) {
        // Left Column
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .background(Color(0x90000000))
                .padding(4.dp)
        ) {
            DebugLine("Minecraft $versionName (Java Edition / Android)")
            DebugLine("60 fps, 16 chunks")
            DebugLine("Integrated server @ 16ms ticks")
            DebugLine(
                String.format(
                    Locale.US,
                    "XYZ: %.3f / %.3f / %.3f",
                    player.x,
                    player.y,
                    player.z
                )
            )
            DebugLine("Block: $blockX $blockY $blockZ")
            DebugLine("Chunk: $chunkX ${blockY / 16} $chunkZ in $blockX $blockY $blockZ")
            DebugLine(String.format(Locale.US, "Facing: %s (%.1f / %.1f)", facing, player.yaw, player.pitch))
            DebugLine("Biome: $biome")
            DebugLine("Light: ${if (world.isDaytime) 15 else 4} (sky, 0 block)")
            DebugLine("Time: ${world.timeOfDay} / 24000")
            DebugLine("GameMode: ${player.gameMode.name}")
        }

        // Right Column
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .background(Color(0x90000000))
                .padding(4.dp)
        ) {
            DebugLine("Java: $javaRuntime")
            DebugLine("Mem: 42% 108/256MB")
            DebugLine("Allocated: 100% 256MB")
            DebugLine("CPU: 8x ARM64 @ 2.8GHz")
            DebugLine("Renderer: OpenGL ES 2.0 (Shader Pipeline)")
            if (raycast.hit) {
                DebugLine("Targeted Block: ${raycast.blockX}, ${raycast.blockY}, ${raycast.blockZ}")
                DebugLine("minecraft:${raycast.blockType.name.lowercase()}")
            } else {
                DebugLine("Targeted Block: None")
            }
        }
    }
}

@Composable
private fun DebugLine(text: String) {
    Text(
        text = text,
        color = Color(0xFFE2E8F0),
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 13.sp
    )
}
