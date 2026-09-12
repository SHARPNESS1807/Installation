package com.example.game.world

import kotlin.math.floor

/**
 * Fast coherent 2D/3D gradient noise for procedural infinite Minecraft Java terrain generation.
 */
class WorldNoise(val seed: Long) {
    private val perm = IntArray(512)

    init {
        val p = IntArray(256) { it }
        var s = seed
        for (i in 255 downTo 1) {
            s = s * 6364136223846793005L + 1442695040888963407L
            val j = (kotlin.math.abs(s) % (i + 1)).toInt()
            val temp = p[i]
            p[i] = p[j]
            p[j] = temp
        }
        for (i in 0 until 512) {
            perm[i] = p[i and 255]
        }
    }

    private fun fade(t: Double): Double = t * t * t * (t * (t * 6 - 15) + 10)
    private fun lerp(t: Double, a: Double, b: Double): Double = a + t * (b - a)

    private fun grad(hash: Int, x: Double, y: Double): Double {
        val h = hash and 7
        val u = if (h < 4) x else y
        val v = if (h < 4) y else x
        return (if ((h and 1) == 0) u else -u) + (if ((h and 2) == 0) v else -v)
    }

    fun perlin2D(x: Double, y: Double): Double {
        val xi = floor(x).toInt() and 255
        val yi = floor(y).toInt() and 255
        val xf = x - floor(x)
        val yf = y - floor(y)

        val u = fade(xf)
        val v = fade(yf)

        val aa = perm[perm[xi] + yi]
        val ab = perm[perm[xi] + yi + 1]
        val ba = perm[perm[xi + 1] + yi]
        val bb = perm[perm[xi + 1] + yi + 1]

        val x1 = lerp(u, grad(aa, xf, yf), grad(ba, xf - 1, yf))
        val x2 = lerp(u, grad(ab, xf, yf - 1), grad(bb, xf - 1, yf - 1))
        return lerp(v, x1, x2)
    }

    /**
     * Multi-octave fractal noise.
     */
    fun fbm2D(x: Double, y: Double, octaves: Int = 3, lacunarity: Double = 2.0, gain: Double = 0.5): Double {
        var total = 0.0
        var frequency = 1.0
        var amplitude = 1.0
        var maxValue = 0.0

        for (i in 0 until octaves) {
            total += perlin2D(x * frequency, y * frequency) * amplitude
            maxValue += amplitude
            amplitude *= gain
            frequency *= lacunarity
        }
        return total / maxValue
    }
}
