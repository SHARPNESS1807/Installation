package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Composable
fun PojavControlsOverlay(
    onDebugClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onKeyboardClick: () -> Unit = {},
    onEscClick: () -> Unit = {},
    onThirdPersonClick: () -> Unit = {},
    onGameModeClick: () -> Unit = {},
    onHitboxesClick: () -> Unit = {},
    onOffhandClick: () -> Unit = {},
    onSecClick: () -> Unit = {},
    onDropClick: () -> Unit = {},
    onShiftClick: () -> Unit = {},
    onJumpClick: () -> Unit = {},
    onPriClick: () -> Unit = {},
    onTabClick: () -> Unit = {},
    onInvClick: () -> Unit = {},
    onSprintClick: () -> Unit = {},
    onMoveInput: ((forward: Float, strafe: Float) -> Unit)? = null,
    isShiftActive: Boolean = false,
    isSprintActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    var isMouseActive by remember { mutableStateOf(false) }
    var mousePos by remember { mutableStateOf(Offset(400f, 300f)) }
    var isTabVisible by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        // TOP BAR: [DEBUG] [CHAT] ... [MOUSE] (ESC and KEYBOARD removed per user request)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 2.dp, top = 2.dp, end = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                PojavBarButton(text = "DEBUG", onClick = onDebugClick)
                PojavBarButton(text = "CHAT", onClick = onChatClick)
            }

            PojavBarButton(
                text = "MOUSE",
                isActive = isMouseActive,
                onClick = { isMouseActive = !isMouseActive }
            )
        }

        // SECOND ROW (Left side): [GAMEMODE] (3RD and HITBOXES removed per user request)
        Row(
            modifier = Modifier
                .padding(start = 2.dp, top = 34.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            PojavBarButton(text = "GAMEMODE", onClick = onGameModeClick)
        }

        // BOTTOM LEFT: Analog Joystick
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 24.dp)
        ) {
            PojavJoystick(
                onMove = { fwd, strafe ->
                    onMoveInput?.invoke(fwd, strafe)
                }
            )
        }

        // BOTTOM RIGHT: 3x3 Keypad Grid
        // [OFFHAND] [SEC]  [DROP]
        // [SHIFT]   [JUMP] [PRI]
        // [TAB]     [INV]  [SPRINT]
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 4.dp, bottom = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Row 1
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                PojavKeypadButton(text = "OFFHAN\nD", onClick = onOffhandClick)
                PojavKeypadButton(text = "SEC", onClick = onSecClick)
                PojavKeypadButton(text = "DROP", onClick = onDropClick)
            }
            // Row 2
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                PojavKeypadButton(text = "SHIFT", isActive = isShiftActive, onClick = onShiftClick)
                PojavKeypadButton(text = "JUMP", onClick = onJumpClick)
                PojavKeypadButton(text = "PRI", onClick = onPriClick)
            }
            // Row 3
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                PojavKeypadButton(
                    text = "TAB",
                    isActive = isTabVisible,
                    onClick = {
                        isTabVisible = !isTabVisible
                        onTabClick()
                    }
                )
                PojavKeypadButton(text = "INV", onClick = onInvClick)
                PojavKeypadButton(text = "SPRINT", isActive = isSprintActive, onClick = onSprintClick)
            }
        }

        // TAB Player List Dialog Overlay
        if (isTabVisible) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 56.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xDD111111))
                    .border(1.5.dp, Color(0xFF666666), RoundedCornerShape(4.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Minecraft 26.2 (Java Edition) • 1/8 Players Online",
                        color = Color(0xFFFFD54F),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .background(Color(0x66333333))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(text = "Player", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = "2000/2000 HP", color = Color(0xFF81C784), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                        Text(text = "📶 15ms", color = Color(0xFF81D4FA), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }
                }
            }
        }

        // VIRTUAL MOUSE CURSOR
        if (isMouseActive) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(mousePos.x.roundToInt(), mousePos.y.roundToInt()) }
                    .size(24.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            mousePos = Offset(
                                (mousePos.x + dragAmount.x).coerceIn(0f, 2000f),
                                (mousePos.y + dragAmount.y).coerceIn(0f, 1000f)
                            )
                        }
                    }
            ) {
                // Classic white arrow cursor
                Text(
                    text = "↖",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.offset(x = (-4).dp, y = (-4).dp)
                )
            }
        }
    }
}

@Composable
fun PojavBarButton(
    text: String,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(28.dp)
            .width(if (text.length > 6) 84.dp else 56.dp)
            .background(if (isActive) Color(0x992E7D32) else Color(0x66000000))
            .border(1.dp, if (isActive) Color(0xFF81C784) else Color(0x33FFFFFF))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PojavKeypadButton(
    text: String,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed) {
            onClick()
        }
    }

    Box(
        modifier = Modifier
            .size(width = 46.dp, height = 42.dp)
            .background(
                when {
                    isPressed -> Color(0xAAFFFFFF)
                    isActive -> Color(0x881B5E20)
                    else -> Color(0x66000000)
                }
            )
            .border(1.dp, if (isActive) Color(0xFF81C784) else Color(0x33FFFFFF))
            .clickable(interactionSource = interactionSource, indication = null) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isPressed) Color.Black else Color.White,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 10.sp
        )
    }
}

@Composable
fun PojavJoystick(
    onMove: (forward: Float, strafe: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var knobOffsetX by remember { mutableFloatStateOf(0f) }
    var knobOffsetY by remember { mutableFloatStateOf(0f) }
    val maxRadius = 40f

    Box(
        modifier = modifier
            .size(92.dp)
            .clip(CircleShape)
            .background(Color(0x55000000))
            .border(1.5.dp, Color(0x55FFFFFF), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
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
                    val newX = knobOffsetX + dragAmount.x
                    val newY = knobOffsetY + dragAmount.y
                    val dist = sqrt(newX * newX + newY * newY)

                    if (dist <= maxRadius) {
                        knobOffsetX = newX
                        knobOffsetY = newY
                    } else {
                        knobOffsetX = (newX / dist) * maxRadius
                        knobOffsetY = (newY / dist) * maxRadius
                    }

                    val strafe = (knobOffsetX / maxRadius).coerceIn(-1f, 1f)
                    val forward = (-knobOffsetY / maxRadius).coerceIn(-1f, 1f)
                    onMove(forward, strafe)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Center white knob
        Box(
            modifier = Modifier
                .offset { IntOffset(knobOffsetX.roundToInt(), knobOffsetY.roundToInt()) }
                .size(30.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}
