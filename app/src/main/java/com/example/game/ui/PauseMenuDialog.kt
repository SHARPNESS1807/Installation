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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.game.audio.SoundManager
import com.example.game.entities.Player
import com.example.game.world.World

@Composable
fun PauseMenuDialog(
    player: Player,
    world: World,
    soundManager: SoundManager,
    fov: Float,
    onFovChange: (Float) -> Unit,
    sensitivity: Float,
    onSensitivityChange: (Float) -> Unit,
    onResume: () -> Unit,
    onOpenCustomControls: () -> Unit = {},
    onSaveAndQuit: () -> Unit
) {
    var showOptions by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onResume,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xBB000000))
                .clickable(enabled = !showOptions, onClick = onResume),
            contentAlignment = Alignment.Center
        ) {
            if (showOptions) {
                com.example.game.ui.options.JavaOptionsScreen(
                    soundManager = soundManager,
                    onBack = {
                        onFovChange(com.example.game.ui.options.GameOptionsState.fov)
                        onSensitivityChange(com.example.game.ui.options.GameOptionsState.lookSensitivity)
                        showOptions = false
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .widthIn(max = 350.dp)
                        .fillMaxWidth(0.92f)
                        .heightIn(max = 420.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF2B2B2B))
                        .border(3.dp, Color(0xFF555555), RoundedCornerShape(6.dp))
                        .clickable(enabled = false) {}
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Game Menu",
                            color = Color.White,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        MinecraftButton(
                            text = "Back to Game",
                            onClick = onResume
                        )

                        MinecraftButton(
                            text = "Teleport to Spawn",
                            onClick = {
                                val spawn = world.getSpawnPosition()
                                player.x = spawn.first
                                player.y = spawn.second
                                player.z = spawn.third
                                onResume()
                            }
                        )

                        MinecraftButton(
                            text = "Custom Controls...",
                            onClick = onOpenCustomControls
                        )

                        MinecraftButton(
                            text = "Options...",
                            onClick = { showOptions = true }
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        MinecraftButton(
                            text = "Disconnect",
                            backgroundColor = Color(0xFFC62828),
                            borderColor = Color(0xFFFF5252),
                            onClick = onSaveAndQuit
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MinecraftButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF5A5A5A),
    borderColor: Color = Color(0xFF8B8B8B)
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(2.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .border(2.dp, borderColor, RoundedCornerShape(2.dp))
    ) {
        Text(
            text = text,
            color = Color.White,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}
