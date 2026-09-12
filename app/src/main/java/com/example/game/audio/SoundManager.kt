package com.example.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

/**
 * Procedural retro sound synthesizer for authentic Minecraft sound effects
 * using Android AudioTrack and tactile haptic feedback.
 */
class SoundManager(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val scope = CoroutineScope(Dispatchers.Default)
    private val sampleRate = 22050

    var isSoundEnabled: Boolean = true
    var isHapticsEnabled: Boolean = true

    fun playStep() {
        if (!isSoundEnabled) return
        scope.launch {
            // Short dry crunch
            val duration = 0.05f
            val numSamples = (sampleRate * duration).toInt()
            val buffer = ShortArray(numSamples)
            val rnd = Random(System.nanoTime())
            for (i in 0 until numSamples) {
                val progress = 1f - (i.toFloat() / numSamples)
                val noise = (rnd.nextFloat() * 2f - 1f) * 0.4f
                val tone = sin(2.0 * Math.PI * 120.0 * i / sampleRate).toFloat() * 0.6f
                val sample = ((noise + tone) * progress * 8000).toInt().coerceIn(-32767, 32767)
                buffer[i] = sample.toShort()
            }
            playPcm(buffer)
        }
    }

    fun playBlockBreak() {
        if (!isSoundEnabled) return
        vibrate(30)
        scope.launch {
            // Snappy breaking crunch
            val duration = 0.12f
            val numSamples = (sampleRate * duration).toInt()
            val buffer = ShortArray(numSamples)
            val rnd = Random(System.nanoTime())
            for (i in 0 until numSamples) {
                val progress = 1f - (i.toFloat() / numSamples)
                val noise = (rnd.nextFloat() * 2f - 1f)
                val bass = sin(2.0 * Math.PI * (180.0 - i * 0.1) * i / sampleRate).toFloat()
                val sample = ((noise * 0.6f + bass * 0.4f) * progress * 14000).toInt().coerceIn(-32767, 32767)
                buffer[i] = sample.toShort()
            }
            playPcm(buffer)
        }
    }

    fun playBlockPlace() {
        if (!isSoundEnabled) return
        vibrate(20)
        scope.launch {
            // Deep solid thud
            val duration = 0.08f
            val numSamples = (sampleRate * duration).toInt()
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val progress = 1f - (i.toFloat() / numSamples)
                val freq = 160.0 - (i.toFloat() / numSamples) * 80.0
                val tone = sin(2.0 * Math.PI * freq * i / sampleRate).toFloat()
                val sample = (tone * progress * 18000).toInt().coerceIn(-32767, 32767)
                buffer[i] = sample.toShort()
            }
            playPcm(buffer)
        }
    }

    fun playHurt() {
        if (!isSoundEnabled) return
        vibrate(80)
        scope.launch {
            // Iconic classic Minecraft hurt pitch bend
            val duration = 0.22f
            val numSamples = (sampleRate * duration).toInt()
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val progress = 1f - (i.toFloat() / numSamples)
                val freq = 280.0 - (i.toFloat() / numSamples) * 140.0
                val square = if (sin(2.0 * Math.PI * freq * i / sampleRate) > 0) 0.5f else -0.5f
                val sample = (square * progress * 16000).toInt().coerceIn(-32767, 32767)
                buffer[i] = sample.toShort()
            }
            playPcm(buffer)
        }
    }

    fun playPop() {
        if (!isSoundEnabled) return
        scope.launch {
            // High cheerful pickup pop
            val duration = 0.06f
            val numSamples = (sampleRate * duration).toInt()
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val progress = 1f - (i.toFloat() / numSamples)
                val freq = 600.0 + (i.toFloat() / numSamples) * 400.0
                val tone = sin(2.0 * Math.PI * freq * i / sampleRate).toFloat()
                val sample = (tone * progress * 14000).toInt().coerceIn(-32767, 32767)
                buffer[i] = sample.toShort()
            }
            playPcm(buffer)
        }
    }

    fun playEat() {
        if (!isSoundEnabled) return
        vibrate(15)
        scope.launch {
            val duration = 0.09f
            val numSamples = (sampleRate * duration).toInt()
            val buffer = ShortArray(numSamples)
            val rnd = Random(System.nanoTime())
            for (i in 0 until numSamples) {
                val progress = 1f - (i.toFloat() / numSamples)
                val noise = (rnd.nextFloat() * 2f - 1f)
                val freq = 450.0 + (rnd.nextFloat() * 100.0)
                val tone = sin(2.0 * Math.PI * freq * i / sampleRate).toFloat() * 0.4f
                val sample = ((noise * 0.6f + tone) * progress * 12000).toInt().coerceIn(-32767, 32767)
                buffer[i] = sample.toShort()
            }
            playPcm(buffer)
        }
    }

    fun playExplosion() {
        if (!isSoundEnabled) return
        vibrate(200)
        scope.launch {
            val duration = 0.5f
            val numSamples = (sampleRate * duration).toInt()
            val buffer = ShortArray(numSamples)
            val rnd = Random(System.nanoTime())
            for (i in 0 until numSamples) {
                val progress = 1f - (i.toFloat() / numSamples)
                val noise = (rnd.nextFloat() * 2f - 1f)
                val bass = sin(2.0 * Math.PI * 65.0 * i / sampleRate).toFloat() * 0.7f
                val sample = ((noise * 0.8f + bass) * progress * 22000).toInt().coerceIn(-32767, 32767)
                buffer[i] = sample.toShort()
            }
            playPcm(buffer)
        }
    }

    fun playHit() {
        if (!isSoundEnabled) return
        vibrate(40)
        scope.launch {
            val duration = 0.1f
            val numSamples = (sampleRate * duration).toInt()
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val progress = 1f - (i.toFloat() / numSamples)
                val freq = 200.0 - (i.toFloat() / numSamples) * 100.0
                val tone = sin(2.0 * Math.PI * freq * i / sampleRate).toFloat()
                buffer[i] = (tone * progress * 16000).toInt().coerceIn(-32767, 32767).toShort()
            }
            playPcm(buffer)
        }
    }

    fun playLevelUp() {
        if (!isSoundEnabled) return
        vibrate(60)
        scope.launch {
            val duration = 0.35f
            val numSamples = (sampleRate * duration).toInt()
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val progress = 1f - (i.toFloat() / numSamples)
                val step = (i.toFloat() / numSamples * 3).toInt()
                val freq = when (step) {
                    0 -> 523.25 // C5
                    1 -> 659.25 // E5
                    else -> 783.99 // G5
                }
                val tone = sin(2.0 * Math.PI * freq * i / sampleRate).toFloat()
                buffer[i] = (tone * progress * 15000).toInt().coerceIn(-32767, 32767).toShort()
            }
            playPcm(buffer)
        }
    }

    fun vibrate(durationMs: Long) {
        if (!isHapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } catch (_: Exception) {
        }
    }

    private fun playPcm(pcm: ShortArray) {
        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(pcm.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(pcm, 0, pcm.size)
            track.play()
            // Cleanup after track finishes
            scope.launch {
                val sleepTime = (pcm.size * 1000L / sampleRate) + 50L
                kotlinx.coroutines.delay(sleepTime)
                try {
                    track.stop()
                    track.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {
        }
    }

    fun playClick() {
        if (!isSoundEnabled) return
        vibrate(10)
        scope.launch {
            // Crisp classic UI click
            val duration = 0.035f
            val numSamples = (sampleRate * duration).toInt()
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val progress = 1f - (i.toFloat() / numSamples)
                val freq = 1200.0 - (i.toFloat() / numSamples) * 400.0
                val tone = sin(2.0 * Math.PI * freq * i / sampleRate).toFloat()
                val sample = (tone * progress * 14000).toInt().coerceIn(-32767, 32767)
                buffer[i] = sample.toShort()
            }
            playPcm(buffer)
        }
    }

    fun release() {
        scope.cancel()
    }
}

