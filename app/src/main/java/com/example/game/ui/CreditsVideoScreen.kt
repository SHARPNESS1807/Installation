package com.example.game.ui

import android.media.MediaPlayer
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import kotlinx.coroutines.delay

/**
 * Screen displaying the official Minecraft MP4 video credits sequence.
 * Plays res/raw/credits.mp4 with authentic UI controls, time tracker, and skip options.
 */
@Composable
fun CreditsVideoScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    var isFinished by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }
    var currentPosMs by remember { mutableIntStateOf(0) }
    var durationMs by remember { mutableIntStateOf(65000) }
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }

    // Auto-hide controls after 4 seconds of inactivity
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying && !isFinished) {
            delay(4000)
            showControls = false
        }
    }

    // Position tracker loop
    LaunchedEffect(isPlaying) {
        while (true) {
            videoViewRef?.let { vv ->
                if (vv.isPlaying) {
                    currentPosMs = vv.currentPosition
                    val dur = vv.duration
                    if (dur > 0) durationMs = dur
                }
            }
            delay(250)
        }
    }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                showControls = !showControls
            }
    ) {
        // Video View
        AndroidView(
            factory = { ctx ->
                val frameLayout = FrameLayout(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }

                val videoView = VideoView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        android.view.Gravity.CENTER
                    )

                    val uri = Uri.parse("android.resource://${ctx.packageName}/${R.raw.credits}")
                    setVideoURI(uri)

                    setOnPreparedListener { mp ->
                        mp.isLooping = false
                        durationMs = mp.duration
                        start()
                        isPlaying = true
                        isFinished = false
                    }

                    setOnCompletionListener {
                        isPlaying = false
                        isFinished = true
                        showControls = true
                    }

                    setOnErrorListener { _, what, extra ->
                        android.util.Log.e("CreditsVideo", "VideoView error: what=$what extra=$extra")
                        true
                    }
                }

                frameLayout.addView(videoView)
                videoViewRef = videoView
                frameLayout
            },
            update = {
                // VideoView updates handled via ref
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay Controls (Header & Footer)
        AnimatedVisibility(
            visible = showControls || isFinished,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x66000000))
                    .padding(16.dp)
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xDD2A2A2A))
                                .border(1.dp, Color(0xFF666666), RoundedCornerShape(4.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "MINECRAFT CREDITS",
                                color = Color(0xFFFFD54F),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Skip / Close Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xDD333333))
                            .border(1.dp, Color(0xFF888888), RoundedCornerShape(4.dp))
                            .clickable {
                                videoViewRef?.stopPlayback()
                                onBack()
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Credits",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "SKIP (ESC)",
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Center Replay Button if finished
                if (isFinished) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xEE222222))
                            .border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(6.dp))
                            .clickable {
                                videoViewRef?.seekTo(0)
                                videoViewRef?.start()
                                isPlaying = true
                                isFinished = false
                            }
                            .padding(horizontal = 24.dp, vertical = 14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Replay",
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "REPLAY CREDITS",
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Bottom Timeline & Play/Pause
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val progress = if (durationMs > 0) {
                        (currentPosMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                    } else 0f

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF81C784),
                        trackColor = Color(0x66FFFFFF)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Play/Pause button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xDD333333))
                                .border(1.dp, Color(0xFF666666), RoundedCornerShape(4.dp))
                                .clickable {
                                    videoViewRef?.let { vv ->
                                        if (vv.isPlaying) {
                                            vv.pause()
                                            isPlaying = false
                                        } else {
                                            vv.start()
                                            isPlaying = true
                                            isFinished = false
                                        }
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isPlaying) "PAUSE" else "PLAY",
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Time label: e.g. 0:24 / 1:05
                        val curSec = (currentPosMs / 1000)
                        val durSec = (durationMs / 1000)
                        val curStr = String.format("%d:%02d", curSec / 60, curSec % 60)
                        val durStr = String.format("%d:%02d", durSec / 60, durSec % 60)

                        Text(
                            text = "$curStr / $durStr",
                            color = Color(0xFFDDDDDD),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
