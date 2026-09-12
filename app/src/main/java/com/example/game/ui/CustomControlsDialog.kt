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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.roundToInt

@Composable
fun CustomControlsDialog(
    currentSettings: ControlSettings,
    onSaveSettings: (ControlSettings) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var dpadScale by remember { mutableFloatStateOf(currentSettings.dpadScale) }
    var buttonScale by remember { mutableFloatStateOf(currentSettings.buttonScale) }
    var opacity by remember { mutableFloatStateOf(currentSettings.opacity) }
    var isLeftHanded by remember { mutableStateOf(currentSettings.isLeftHanded) }
    var controlStyle by remember { mutableStateOf(currentSettings.controlStyle) }
    var showInGameDisconnect by remember { mutableStateOf(currentSettings.showInGameDisconnect) }
    var dpadOffsetX by remember { mutableFloatStateOf(currentSettings.dpadOffsetX) }
    var dpadOffsetY by remember { mutableFloatStateOf(currentSettings.dpadOffsetY) }
    var actionsOffsetX by remember { mutableFloatStateOf(currentSettings.actionsOffsetX) }
    var actionsOffsetY by remember { mutableFloatStateOf(currentSettings.actionsOffsetY) }
    var hapticFeedback by remember { mutableStateOf(currentSettings.hapticFeedback) }

    var selectedTab by remember { mutableStateOf("GENERAL") } // "GENERAL", "POSITION", "PREVIEW"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xCC000000))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(520.dp)
                    .height(340.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF262626))
                    .border(3.dp, Color(0xFF5A5A5A), RoundedCornerShape(8.dp))
                    .clickable(enabled = false) {}
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Tune,
                                contentDescription = "Custom Controls",
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Custom Controls Settings",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Tab selector
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TabButton("General", selectedTab == "GENERAL") { selectedTab = "GENERAL" }
                            TabButton("Offsets", selectedTab == "POSITION") { selectedTab = "POSITION" }
                            TabButton("Preview", selectedTab == "PREVIEW") { selectedTab = "PREVIEW" }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tab Content
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1B1B1B))
                            .border(1.dp, Color(0xFF3D3D3D), RoundedCornerShape(6.dp))
                            .padding(12.dp)
                    ) {
                        when (selectedTab) {
                            "GENERAL" -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // D-Pad / Movement Size
                                    SliderControlRow(
                                        label = "D-Pad / Joystick Scale",
                                        value = dpadScale,
                                        valueText = "${(dpadScale * 100).roundToInt()}%",
                                        range = 0.7f..1.5f,
                                        onValueChange = { dpadScale = it }
                                    )

                                    // Action Button Size
                                    SliderControlRow(
                                        label = "Action Buttons Scale",
                                        value = buttonScale,
                                        valueText = "${(buttonScale * 100).roundToInt()}%",
                                        range = 0.7f..1.5f,
                                        onValueChange = { buttonScale = it }
                                    )

                                    // Opacity / Transparency
                                    SliderControlRow(
                                        label = "Button Opacity",
                                        value = opacity,
                                        valueText = "${(opacity * 100).roundToInt()}%",
                                        range = 0.3f..1.0f,
                                        onValueChange = { opacity = it }
                                    )

                                    // In-Game Disconnect Button
                                    SwitchControlRow(
                                        title = "Render In-Game Disconnect Button",
                                        subtitle = "Show red Disconnect button on top in-game HUD",
                                        checked = showInGameDisconnect,
                                        onCheckedChange = { showInGameDisconnect = it }
                                    )

                                    // Left-Handed Mode
                                    SwitchControlRow(
                                        title = "Left-Handed Mode (Swap Sides)",
                                        subtitle = "Place movement stick on right, action buttons on left",
                                        checked = isLeftHanded,
                                        onCheckedChange = { isLeftHanded = it }
                                    )

                                    // Control Style: D-Pad vs Joystick
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Movement Control Style",
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = if (controlStyle == ControlStyle.DPAD) "Classic 4-Way D-Pad" else "Smooth Analog Joystick",
                                                color = Color(0xFFAAAAAA),
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            StyleButton(
                                                label = "D-PAD",
                                                selected = controlStyle == ControlStyle.DPAD
                                            ) {
                                                controlStyle = ControlStyle.DPAD
                                            }
                                            StyleButton(
                                                label = "JOYSTICK",
                                                selected = controlStyle == ControlStyle.JOYSTICK
                                            ) {
                                                controlStyle = ControlStyle.JOYSTICK
                                            }
                                        }
                                    }

                                    // Haptic Feedback
                                    SwitchControlRow(
                                        title = "Vibration & Haptic Feedback",
                                        subtitle = "Haptic pulse when mining and placing blocks",
                                        checked = hapticFeedback,
                                        onCheckedChange = { hapticFeedback = it }
                                    )
                                }
                            }
                            "POSITION" -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Nudge & Offset Control Locations",
                                        color = Color(0xFFFFD54F),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )

                                    SliderControlRow(
                                        label = "D-Pad Horizontal Offset (X)",
                                        value = dpadOffsetX,
                                        valueText = "${dpadOffsetX.roundToInt()} dp",
                                        range = -40f..50f,
                                        onValueChange = { dpadOffsetX = it }
                                    )

                                    SliderControlRow(
                                        label = "D-Pad Vertical Offset (Y)",
                                        value = dpadOffsetY,
                                        valueText = "${dpadOffsetY.roundToInt()} dp",
                                        range = -40f..40f,
                                        onValueChange = { dpadOffsetY = it }
                                    )

                                    SliderControlRow(
                                        label = "Action Buttons Horizontal Offset (X)",
                                        value = actionsOffsetX,
                                        valueText = "${actionsOffsetX.roundToInt()} dp",
                                        range = -40f..50f,
                                        onValueChange = { actionsOffsetX = it }
                                    )

                                    SliderControlRow(
                                        label = "Action Buttons Vertical Offset (Y)",
                                        value = actionsOffsetY,
                                        valueText = "${actionsOffsetY.roundToInt()} dp",
                                        range = -40f..40f,
                                        onValueChange = { actionsOffsetY = it }
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Button(
                                            onClick = {
                                                dpadOffsetX = 0f
                                                dpadOffsetY = 0f
                                                actionsOffsetX = 0f
                                                actionsOffsetY = 0f
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF424242)),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text("Reset Offsets to 0", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                }
                            }
                            "PREVIEW" -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFF141414)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Preview HUD frame
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .border(1.dp, Color(0xFF333333))
                                    ) {
                                        // Top Bar Preview
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                PreviewBadge("ESC")
                                                PreviewBadge("F3")
                                                PreviewBadge("Chat")
                                            }

                                            if (showInGameDisconnect) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(3.dp))
                                                        .background(Color(0xFFC62828))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text("Disconnect", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                                    }
                                                }
                                            }

                                            PreviewBadge("Inv (E)")
                                        }

                                        // Left vs Right based on isLeftHanded
                                        val moveAlignment = if (isLeftHanded) Alignment.BottomEnd else Alignment.BottomStart
                                        val actionAlignment = if (isLeftHanded) Alignment.BottomStart else Alignment.BottomEnd

                                        // Movement Preview
                                        Box(
                                            modifier = Modifier
                                                .align(moveAlignment)
                                                .padding(
                                                    start = if (!isLeftHanded) (12 + dpadOffsetX).coerceAtLeast(0f).dp else 8.dp,
                                                    end = if (isLeftHanded) (12 + dpadOffsetX).coerceAtLeast(0f).dp else 8.dp,
                                                    bottom = (8 - dpadOffsetY).coerceAtLeast(0f).dp
                                                )
                                        ) {
                                            if (controlStyle == ControlStyle.DPAD) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    modifier = Modifier.padding(4.dp)
                                                ) {
                                                    PreviewButton("▲", dpadScale, opacity)
                                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        PreviewButton("◀", dpadScale, opacity)
                                                        PreviewButton("◆", dpadScale, opacity, Color(0xFF2E7D32))
                                                        PreviewButton("▶", dpadScale, opacity)
                                                    }
                                                    PreviewButton("▼", dpadScale, opacity)
                                                }
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size((90 * dpadScale).dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0x66000000).copy(alpha = opacity * 0.4f))
                                                        .border(2.dp, Color.White.copy(alpha = opacity), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size((40 * dpadScale).dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFF757575).copy(alpha = opacity))
                                                            .border(1.dp, Color.White, CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("JOY", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }

                                        // Actions Preview
                                        Column(
                                            modifier = Modifier
                                                .align(actionAlignment)
                                                .padding(
                                                    start = if (isLeftHanded) (12 + actionsOffsetX).coerceAtLeast(0f).dp else 8.dp,
                                                    end = if (!isLeftHanded) (12 + actionsOffsetX).coerceAtLeast(0f).dp else 8.dp,
                                                    bottom = (8 - actionsOffsetY).coerceAtLeast(0f).dp
                                                ),
                                            horizontalAlignment = if (isLeftHanded) Alignment.Start else Alignment.End,
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            PreviewActionButton("MINE", Icons.Default.Handyman, Color(0xFFB71C1C), buttonScale, opacity)
                                            PreviewActionButton("PLACE", Icons.Default.Construction, Color(0xFF1565C0), buttonScale, opacity)
                                            PreviewActionButton("JUMP", Icons.Default.KeyboardArrowUp, Color(0xFF424242), buttonScale, opacity)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Bottom Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                dpadScale = 1.0f
                                buttonScale = 1.0f
                                opacity = 0.85f
                                isLeftHanded = false
                                controlStyle = ControlStyle.DPAD
                                showInGameDisconnect = true
                                dpadOffsetX = 0f
                                dpadOffsetY = 0f
                                actionsOffsetX = 0f
                                actionsOffsetY = 0f
                                hapticFeedback = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A4A4A)),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset Defaults", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF555555)),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("Cancel", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            }

                            Button(
                                onClick = {
                                    val newSettings = ControlSettings(
                                        dpadScale = dpadScale,
                                        buttonScale = buttonScale,
                                        opacity = opacity,
                                        isLeftHanded = isLeftHanded,
                                        controlStyle = controlStyle,
                                        showInGameDisconnect = showInGameDisconnect,
                                        dpadOffsetX = dpadOffsetX,
                                        dpadOffsetY = dpadOffsetY,
                                        actionsOffsetX = actionsOffsetX,
                                        actionsOffsetY = actionsOffsetY,
                                        hapticFeedback = hapticFeedback
                                    )
                                    ControlSettings.save(context, newSettings)
                                    onSaveSettings(newSettings)
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("Save & Apply", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabButton(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (selected) Color(0xFF4E6B34) else Color(0xFF333333))
            .border(1.dp, if (selected) Color(0xFF8BC34A) else Color(0xFF555555), RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color(0xFFAAAAAA),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun StyleButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (selected) Color(0xFF1B5E20) else Color(0xFF333333))
            .border(1.5.dp, if (selected) Color(0xFF81C784) else Color(0xFF555555), RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color(0xFFBBBBBB),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun SliderControlRow(
    label: String,
    value: Float,
    valueText: String,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            Text(text = valueText, color = Color(0xFFFFD54F), fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFFFD54F),
                activeTrackColor = Color(0xFF81C784),
                inactiveTrackColor = Color(0xFF424242)
            ),
            modifier = Modifier.height(28.dp)
        )
    }
}

@Composable
private fun SwitchControlRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Text(text = subtitle, color = Color(0xFFAAAAAA), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF81C784),
                checkedTrackColor = Color(0xFF2E7D32),
                uncheckedThumbColor = Color(0xFF9E9E9E),
                uncheckedTrackColor = Color(0xFF424242)
            )
        )
    }
}

@Composable
private fun PreviewBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(2.dp))
            .background(Color(0x88000000))
            .border(0.8.dp, Color(0x66FFFFFF), RoundedCornerShape(2.dp))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(text = text, color = Color.White, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun PreviewButton(text: String, scale: Float, opacity: Float, bg: Color = Color(0x88000000)) {
    Box(
        modifier = Modifier
            .size((28 * scale).dp)
            .clip(CircleShape)
            .background(bg.copy(alpha = opacity * 0.8f))
            .border(1.dp, Color.White.copy(alpha = opacity), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White.copy(alpha = opacity), fontSize = (11 * scale).sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PreviewActionButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, bg: Color, scale: Float, opacity: Float) {
    Box(
        modifier = Modifier
            .size((34 * scale).dp)
            .clip(CircleShape)
            .background(bg.copy(alpha = opacity))
            .border(1.dp, Color.White.copy(alpha = opacity), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size((16 * scale).dp))
    }
}
