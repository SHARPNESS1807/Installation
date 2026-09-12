package com.example.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.entities.GameMode
import com.example.game.entities.Player
import com.example.game.world.ItemStack
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinecraftHUD(
    player: Player,
    mobs: List<com.example.game.entities.Mob> = emptyList(),
    controlSettings: ControlSettings = ControlSettings(),
    versionName: String = "26.2",
    onMoveInput: (forward: Float, strafe: Float) -> Unit,
    onLookDrag: (deltaX: Float, deltaY: Float) -> Unit,
    onJump: () -> Unit,
    onMine: () -> Unit,
    onPlace: () -> Unit,
    onOpenInventory: () -> Unit,
    onOpenPause: () -> Unit,
    onToggleF3: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenCustomControls: () -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedItemName by remember { mutableStateOf("") }
    var showItemNameBanner by remember { mutableStateOf(false) }

    val bossDragon = mobs.firstOrNull { it.type == com.example.game.entities.MobType.ENDER_DRAGON && it.isAlive }

    LaunchedEffect(player.selectedHotbarSlot) {
        val item = player.selectedItemStack?.item
        if (item != null) {
            selectedItemName = item.displayName
            showItemNameBanner = true
            delay(1800)
            showItemNameBanner = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Ender Dragon Boss Bar
        if (bossDragon != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Ender Dragon",
                    color = Color(0xFFEA80FC),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(280.dp)
                        .height(14.dp)
                        .background(Color(0xFF1E0A24), RoundedCornerShape(3.dp))
                        .border(1.5.dp, Color(0xFF6A1B9A), RoundedCornerShape(3.dp))
                ) {
                    val hpRatio = (bossDragon.health.toFloat() / bossDragon.type.maxHealth.toFloat()).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .fillMaxWidth(hpRatio)
                            .background(Color(0xFFBA68C8), RoundedCornerShape(2.dp))
                    )
                }
            }
        }
        // Center Crosshair & Attack Cooldown Indicator
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Horizontal bar
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(2.5.dp)
                    .background(Color(0xD0FFFFFF))
            )
            // Vertical bar
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .height(16.dp)
                    .background(Color(0xD0FFFFFF))
            )
        }

        // Java Edition Attack Cooldown Indicator
        if (player.attackCooldown < 0.98f) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 34.dp)
                    .width(26.dp)
                    .height(4.dp)
                    .background(Color(0x99000000))
                    .border(0.8.dp, Color(0x66FFFFFF))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .fillMaxWidth(player.attackCooldown.coerceIn(0f, 1f))
                        .background(Color(0xFFE0E0E0))
                )
            }
        }

        // Survival Block Mining Delay & Crack Progress Indicator
        if (player.miningProgress > 0f) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 28.dp)
                    .width(36.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xCC000000))
                    .border(1.dp, Color(0xFFFFD54F), RoundedCornerShape(2.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(player.miningProgress.coerceIn(0f, 1f))
                        .background(Color(0xFFFFB300))
                )
            }
        }

        // Java Advancement Toast Notification
        val activeToast = com.example.game.advancements.AdvancementManager.activeToast
        if (activeToast != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 40.dp, end = 16.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xF01E1E1E))
                    .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(6.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = activeToast.icon, fontSize = 24.sp)
                    Column {
                        Text(
                            text = "Advancement Made!",
                            color = Color(0xFFFFAA00),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = activeToast.title,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        var showKeyboardDialog by remember { mutableStateOf(false) }
        var keyboardInputText by remember { mutableStateOf("") }

        // Pojav Controls Overlay (DEBUG, CHAT, KEYBOARD, ESC, MOUSE, 3RD, GAMEMODE, HITBOXES, Joystick, 3x3 Keypad)
        PojavControlsOverlay(
            onDebugClick = onToggleF3,
            onChatClick = onOpenChat,
            onKeyboardClick = { showKeyboardDialog = true },
            onEscClick = onOpenPause,
            onThirdPersonClick = { player.cycleCameraPerspective() },
            onGameModeClick = { player.cycleGameMode() },
            onHitboxesClick = { player.showHitboxes = !player.showHitboxes },
            onOffhandClick = { player.swapHands() },
            onSecClick = onPlace,
            onDropClick = { player.dropSelectedItem() },
            onShiftClick = { player.isSneaking = !player.isSneaking },
            onJumpClick = onJump,
            onPriClick = onMine,
            onTabClick = {},
            onInvClick = onOpenInventory,
            onSprintClick = { player.isSprinting = !player.isSprinting },
            onMoveInput = onMoveInput,
            isShiftActive = player.isSneaking,
            isSprintActive = player.isSprinting
        )

        // Status indicator badges for Hitboxes & Camera Perspective
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (player.showHitboxes) {
                Box(
                    modifier = Modifier
                        .background(Color(0xBB000000), RoundedCornerShape(3.dp))
                        .border(1.dp, Color(0xFF81C784), RoundedCornerShape(3.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("Hitboxes: ON", color = Color(0xFF81C784), fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
            if (player.cameraPerspective != com.example.game.entities.Player.CameraPerspective.FIRST_PERSON) {
                Box(
                    modifier = Modifier
                        .background(Color(0xBB000000), RoundedCornerShape(3.dp))
                        .border(1.dp, Color(0xFF64B5F6), RoundedCornerShape(3.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (player.cameraPerspective == com.example.game.entities.Player.CameraPerspective.THIRD_PERSON_BACK) "Perspective: 3rd Person Back" else "Perspective: 3rd Person Front",
                        color = Color(0xFF64B5F6),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Selected Item Name Popup Banner
        AnimatedVisibility(
            visible = showItemNameBanner,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 126.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xB0000000))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = selectedItemName,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Center Stats + Hotbar
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 8.dp)
        ) {

            // Center: Stats + Hotbar
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Hearts, Food, Exp bar (in Survival or Hardcore)
                if (player.gameMode == GameMode.SURVIVAL || player.gameMode == GameMode.HARDCORE) {
                    SurvivalStatsRow(
                        health = player.health,
                        maxHealth = player.maxHealth,
                        hunger = player.hunger,
                        expLevel = player.expLevel,
                        expProgress = player.expProgress,
                        isHurt = player.hurtTimer > 0f,
                        isHardcore = player.gameMode == GameMode.HARDCORE
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (player.gameMode == GameMode.SPECTATOR) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xAA000000))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "SPECTATOR MODE • Noclip Enabled",
                            color = Color(0xFFE1BEE7),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // 9 Hotbar Slots
                    JavaHotbar(
                        player = player,
                        onSelectSlot = { player.selectedHotbarSlot = it }
                    )
                }
            }
        }

        if (showKeyboardDialog) {
            BasicAlertDialog(onDismissRequest = { showKeyboardDialog = false }) {
                Box(
                    modifier = Modifier
                        .width(360.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF222222))
                        .border(2.dp, Color(0xFF666666), RoundedCornerShape(4.dp))
                        .padding(14.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Keyboard / Command", color = Color(0xFFFFD54F), fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = keyboardInputText,
                            onValueChange = { keyboardInputText = it },
                            placeholder = { Text("/gamemode creative, /time set day...", color = Color.Gray, fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF4CAF50),
                                unfocusedBorderColor = Color.Gray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .background(Color(0xFF4E4E4E))
                                    .border(1.5.dp, Color(0xFF7A7A7A))
                                    .clickable {
                                        if (keyboardInputText.isNotBlank()) {
                                            when (keyboardInputText.trim().lowercase()) {
                                                "/gamemode creative", "/gamemode c" -> player.gameMode = GameMode.CREATIVE
                                                "/gamemode survival", "/gamemode s" -> player.gameMode = GameMode.SURVIVAL
                                                "/gamemode spectator", "/gamemode sp" -> player.gameMode = GameMode.SPECTATOR
                                                "/gamemode hardcore", "/gamemode h" -> player.gameMode = GameMode.HARDCORE
                                                "/heal" -> player.heal(player.maxHealth)
                                            }
                                            keyboardInputText = ""
                                        }
                                        showKeyboardDialog = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Send", color = Color.White, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .background(Color(0xFF333333))
                                    .border(1.5.dp, Color(0xFF666666))
                                    .clickable { showKeyboardDialog = false },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Close", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SurvivalStatsRow(
    health: Int,
    maxHealth: Int = 2000,
    hunger: Int,
    expLevel: Int,
    expProgress: Float,
    isHurt: Boolean,
    isHardcore: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(380.dp)
    ) {
        // Hearts on Left, Drumsticks on Right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hearts row (scales with maxHealth)
            Row(
                horizontalArrangement = Arrangement.spacedBy(1.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val stepHp = (maxHealth.toFloat() / 10f).coerceAtLeast(2f)
                for (i in 0 until 10) {
                    val heartHp = (i + 1) * stepHp
                    val heartColor = when {
                        isHurt -> Color(0xFFFFCDD2)
                        health >= heartHp -> if (isHardcore) Color(0xFFC62828) else Color(0xFFE53935)
                        health >= heartHp - (stepHp / 2f) -> if (isHardcore) Color(0xFFEF5350) else Color(0xFFEF5350)
                        else -> Color(0x55424242)
                    }
                    val heartSymbol = if (isHardcore) "🖤" else "❤"
                    Text(
                        text = heartSymbol,
                        color = heartColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$health HP",
                    color = Color(0xFFFFD54F),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }

            // 10 Food icons (20 Hunger)
            Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                for (i in 0 until 10) {
                    val foodPoints = (i + 1) * 2
                    val drumstickColor = if (hunger >= foodPoints) Color(0xFFD87C38) else Color(0x55424242)
                    Text(
                        text = "🍗",
                        color = drumstickColor,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Experience Level Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            contentAlignment = Alignment.Center
        ) {
            LinearProgressIndicator(
                progress = { expProgress.coerceIn(0f, 1f) },
                color = Color(0xFF76FF03),
                trackColor = Color(0x66000000),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(2.dp))
            )
            if (expLevel > 0) {
                Text(
                    text = "$expLevel",
                    color = Color(0xFF76FF03),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun JavaHotbar(
    player: Player,
    onSelectSlot: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Java Off-hand Slot
        val offhandStack = player.offhandItem
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF505050))
                .border(2.dp, Color(0xFF9E9E9E), RoundedCornerShape(4.dp))
                .clickable { player.swapHands() }
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            if (offhandStack != null) {
                ItemIcon(item = offhandStack.item, size = 28.dp)
                if (offhandStack.count > 1) {
                    Text(
                        text = "${offhandStack.count}",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 1.dp, bottom = 0.dp)
                    )
                }
            } else {
                Text(
                    text = "OFF\n[F]",
                    color = Color(0x99FFFFFF),
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    lineHeight = 9.sp
                )
            }
        }

        // 9 Hotbar Slots
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF8F8F8F))
                .border(2.dp, Color(0xFF373737), RoundedCornerShape(4.dp))
                .padding(2.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                for (i in 0..8) {
                    val isSelected = player.selectedHotbarSlot == i
                    val stack = player.inventory[i]
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isSelected) Color(0xFFC0C0C0) else Color(0xFF757575))
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) Color.White else Color(0xFF424242),
                                shape = RoundedCornerShape(2.dp)
                            )
                            .clickable { onSelectSlot(i) }
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
            }
        }
    }
}

@Composable
private fun VirtualAnalogJoystick(
    onMove: (forward: Float, strafe: Float) -> Unit,
    scale: Float,
    opacity: Float,
    isSneaking: Boolean,
    onToggleSneak: () -> Unit,
    modifier: Modifier = Modifier
) {
    var knobOffsetX by remember { mutableFloatStateOf(0f) }
    var knobOffsetY by remember { mutableFloatStateOf(0f) }
    val maxRadiusPx = 56f * scale
    val baseSize = (130 * scale).dp
    val knobSize = (50 * scale).dp

    Box(
        modifier = modifier
            .size(baseSize)
            .clip(CircleShape)
            .background(Color(0x77000000).copy(alpha = (opacity * 0.5f).coerceIn(0.1f, 1f)))
            .border(2.dp, Color.White.copy(alpha = opacity), CircleShape)
            .pointerInput(scale) {
                detectDragGestures(
                    onDragStart = {},
                    onDragEnd = {
                        knobOffsetX = 0f
                        knobOffsetY = 0f
                        onMove(0f, 0f)
                    },
                    onDragCancel = {
                        knobOffsetX = 0f
                        knobOffsetY = 0f
                        onMove(0f, 0f)
                    }
                ) { change, dragAmount ->
                    change.consume()
                    knobOffsetX = (knobOffsetX + dragAmount.x).coerceIn(-maxRadiusPx, maxRadiusPx)
                    knobOffsetY = (knobOffsetY + dragAmount.y).coerceIn(-maxRadiusPx, maxRadiusPx)

                    // Normalized movement: Forward is negative Y, strafe is positive X
                    val strafe = (knobOffsetX / maxRadiusPx).coerceIn(-1f, 1f)
                    val forward = (-knobOffsetY / maxRadiusPx).coerceIn(-1f, 1f)
                    onMove(forward, strafe)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Sneak toggle badge in top-right of joystick base
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size((28 * scale).dp)
                .clip(CircleShape)
                .background(if (isSneaking) Color(0xFF2E7D32) else Color(0x99000000))
                .border(1.dp, Color.White.copy(alpha = opacity), CircleShape)
                .clickable(onClick = onToggleSneak),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isSneaking) "S" else "◆",
                color = Color.White,
                fontSize = (9 * scale).sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Draggable Joystick Knob
        Box(
            modifier = Modifier
                .offset(x = knobOffsetX.dp, y = knobOffsetY.dp)
                .size(knobSize)
                .clip(CircleShape)
                .background(Color(0xFF888888).copy(alpha = opacity))
                .border(2.dp, Color.White.copy(alpha = opacity), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size((14 * scale).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = opacity))
            )
        }
    }
}

@Composable
private fun VirtualDPad(
    onMove: (forward: Float, strafe: Float) -> Unit,
    isSneaking: Boolean,
    onToggleSneak: () -> Unit,
    scale: Float = 1.0f,
    opacity: Float = 0.85f,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Forward
        PadButton("▲", scale, opacity) { onMove(1f, 0f) }

        Row(horizontalArrangement = Arrangement.spacedBy((6 * scale).dp)) {
            // Left
            PadButton("◀", scale, opacity) { onMove(0f, -1f) }
            // Center Sneak Toggle
            Box(
                modifier = Modifier
                    .size((46 * scale).dp)
                    .clip(CircleShape)
                    .background(if (isSneaking) Color(0xFF2E7D32).copy(alpha = opacity) else Color(0x88000000).copy(alpha = opacity))
                    .border(2.dp, Color.White.copy(alpha = opacity), CircleShape)
                    .clickable(onClick = onToggleSneak),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isSneaking) "SNEAK" else "◆",
                    color = Color.White.copy(alpha = opacity),
                    fontSize = (9 * scale).sp,
                    fontWeight = FontWeight.Bold
                )
            }
            // Right
            PadButton("▶", scale, opacity) { onMove(0f, 1f) }
        }

        // Backward
        PadButton("▼", scale, opacity) { onMove(-1f, 0f) }
    }
}

@Composable
private fun PadButton(label: String, scale: Float = 1.0f, opacity: Float = 0.85f, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size((46 * scale).dp)
            .clip(CircleShape)
            .background(Color(0x88000000).copy(alpha = opacity))
            .border(2.dp, Color(0xAAFFFFFF).copy(alpha = opacity), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = opacity),
            fontSize = (18 * scale).sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun RightActionControls(
    isFlying: Boolean,
    isCreative: Boolean,
    isSpectator: Boolean = false,
    scale: Float = 1.0f,
    opacity: Float = 0.85f,
    isLeftHanded: Boolean = false,
    onJump: () -> Unit,
    onMine: () -> Unit,
    onPlace: () -> Unit,
    onToggleFly: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = if (isLeftHanded) Alignment.Start else Alignment.End,
        verticalArrangement = Arrangement.spacedBy((8 * scale).dp),
        modifier = modifier
    ) {
        if (!isSpectator) {
            // Mine / Break button
            ActionButton(
                label = "MINE",
                icon = Icons.Default.Handyman,
                backgroundColor = Color(0xFFB71C1C),
                scale = scale,
                opacity = opacity,
                onClick = onMine
            )

            // Place Block button
            ActionButton(
                label = "PLACE",
                icon = Icons.Default.Construction,
                backgroundColor = Color(0xFF1565C0),
                scale = scale,
                opacity = opacity,
                onClick = onPlace
            )
        }

        // Jump / Fly
        Row(horizontalArrangement = Arrangement.spacedBy((6 * scale).dp)) {
            if (isCreative && !isSpectator) {
                ActionButton(
                    label = if (isFlying) "FLY ON" else "FLY",
                    icon = Icons.Default.KeyboardArrowUp,
                    backgroundColor = if (isFlying) Color(0xFF00897B) else Color(0x88000000),
                    scale = scale,
                    opacity = opacity,
                    onClick = onToggleFly
                )
            }
            ActionButton(
                label = if (isSpectator) "ASCEND" else "JUMP",
                icon = Icons.Default.KeyboardArrowUp,
                backgroundColor = if (isSpectator) Color(0xFF6A1B9A) else Color(0x99000000),
                scale = scale,
                opacity = opacity,
                onClick = onJump
            )
        }
    }
}

@Composable
private fun ActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    scale: Float = 1.0f,
    opacity: Float = 0.85f,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size((52 * scale).dp)
            .clip(CircleShape)
            .background(backgroundColor.copy(alpha = opacity))
            .border(2.dp, Color.White.copy(alpha = opacity), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                icon,
                contentDescription = label,
                tint = Color.White.copy(alpha = opacity),
                modifier = Modifier.size((20 * scale).dp)
            )
            Text(
                text = label,
                color = Color.White.copy(alpha = opacity),
                fontSize = (8 * scale).sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun HudTopButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    backgroundColor: Color = Color(0x99000000),
    borderColor: Color = Color(0x88FFFFFF),
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(16.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
