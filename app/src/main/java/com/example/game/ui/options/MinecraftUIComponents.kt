package com.example.game.ui.options

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun JavaOptionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 32.dp,
    textColor: Color = Color.White
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val bgColor = when {
        !enabled -> Color(0xFF333333)
        isPressed -> Color(0xFF3F3F3F)
        else -> Color(0xFF4C4C4C)
    }

    val topHighlight = if (enabled) Color(0xFF8E8E8E) else Color(0xFF555555)
    val bottomShadow = if (enabled) Color(0xFF222222) else Color(0xFF1B1B1B)

    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(1.dp))
            .background(bgColor)
            .drawBehind {
                // Top border highlight
                drawRect(topHighlight, Offset.Zero, Size(size.width, 2.dp.toPx()))
                // Left border highlight
                drawRect(topHighlight, Offset.Zero, Size(2.dp.toPx(), size.height))
                // Bottom border shadow
                drawRect(bottomShadow, Offset(0f, size.height - 2.dp.toPx()), Size(size.width, 2.dp.toPx()))
                // Right border shadow
                drawRect(bottomShadow, Offset(size.width - 2.dp.toPx(), 0f), Size(2.dp.toPx(), size.height))
            }
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) textColor else Color(0xFFAAAAAA),
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
fun JavaOptionSlider(
    label: String,
    value: Float, // 0.0f .. 1.0f
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 32.dp
) {
    BoxWithConstraints(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(1.dp))
            .background(Color(0xFF2C2C2C))
            .drawBehind {
                // Outer button bevel
                drawRect(Color(0xFF666666), Offset.Zero, Size(size.width, 2.dp.toPx()))
                drawRect(Color(0xFF666666), Offset.Zero, Size(2.dp.toPx(), size.height))
                drawRect(Color(0xFF1E1E1E), Offset(0f, size.height - 2.dp.toPx()), Size(size.width, 2.dp.toPx()))
                drawRect(Color(0xFF1E1E1E), Offset(size.width - 2.dp.toPx(), 0f), Size(2.dp.toPx(), size.height))
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val progress = (offset.x / size.width).coerceIn(0f, 1f)
                    onValueChange(progress)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val progress = (change.position.x / size.width).coerceIn(0f, 1f)
                    onValueChange(progress)
                }
            }
    ) {
        val handleWidth = 10.dp
        val maxOffset = maxWidth - handleWidth
        val currentOffset = maxOffset * value.coerceIn(0f, 1f)

        // Draggable Slider Handle
        Box(
            modifier = Modifier
                .offset(x = currentOffset)
                .width(handleWidth)
                .fillMaxHeight()
                .background(Color(0xFF6E6E6E))
                .drawBehind {
                    drawRect(Color(0xFFAAAAAA), Offset.Zero, Size(size.width, 2.dp.toPx()))
                    drawRect(Color(0xFFAAAAAA), Offset.Zero, Size(2.dp.toPx(), size.height))
                    drawRect(Color(0xFF333333), Offset(0f, size.height - 2.dp.toPx()), Size(size.width, 2.dp.toPx()))
                    drawRect(Color(0xFF333333), Offset(size.width - 2.dp.toPx(), 0f), Size(2.dp.toPx(), size.height))
                }
        )

        // Centered Text Label
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
fun JavaSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = Color(0xFFAAAAAA),
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun JavaOptionsScreenScaffold(
    title: String,
    onDone: () -> Unit,
    extraBottomButton: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xEE1E1612)) // Dark Minecraft Dirt/Stone Backdrop
            .padding(top = 10.dp, bottom = 10.dp, start = 12.dp, end = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Screen Title
            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Scrollable / Centered body
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(0.92f),
                contentAlignment = Alignment.TopCenter
            ) {
                content()
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Bottom action row
            Row(
                modifier = Modifier.fillMaxWidth(0.85f),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (extraBottomButton != null) {
                    Box(modifier = Modifier.weight(1f)) {
                        extraBottomButton()
                    }
                }
                JavaOptionButton(
                    text = "Done",
                    onClick = onDone,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
