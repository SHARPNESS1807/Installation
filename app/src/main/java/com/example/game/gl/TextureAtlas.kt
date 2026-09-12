package com.example.game.gl

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.opengl.GLES20
import android.opengl.GLUtils
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.util.Random

/**
 * Procedural texture atlas generating authentic pixel-perfect official Minecraft textures.
 * Dimensions: 256x256 (16x16 tiles, 16 pixels each).
 */
object TextureAtlas {
    const val ATLAS_SIZE = 256
    const val TILE_SIZE = 16
    const val TILES_PER_ROW = ATLAS_SIZE / TILE_SIZE // 16

    var textureId: Int = -1
        private set

    lateinit var atlasBitmap: Bitmap
        private set

    private val tileBitmapCache = mutableMapOf<Int, ImageBitmap>()

    fun getOrCreateAtlasBitmap(): Bitmap {
        if (!::atlasBitmap.isInitialized) {
            atlasBitmap = generateAtlas()
        }
        return atlasBitmap
    }

    fun getTileImageBitmap(tileIdx: Int): ImageBitmap {
        val cached = tileBitmapCache[tileIdx]
        if (cached != null) return cached

        val atlas = getOrCreateAtlasBitmap()
        val col = tileIdx % TILES_PER_ROW
        val row = tileIdx / TILES_PER_ROW
        val sx = (col * TILE_SIZE).coerceIn(0, ATLAS_SIZE - TILE_SIZE)
        val sy = (row * TILE_SIZE).coerceIn(0, ATLAS_SIZE - TILE_SIZE)
        val sub = Bitmap.createBitmap(atlas, sx, sy, TILE_SIZE, TILE_SIZE)
        val imgBitmap = sub.asImageBitmap()
        tileBitmapCache[tileIdx] = imgBitmap
        return imgBitmap
    }

    fun getUV(tileIdx: Int): FloatArray {
        val col = tileIdx % TILES_PER_ROW
        val row = tileIdx / TILES_PER_ROW
        val u0 = col.toFloat() / TILES_PER_ROW
        val v0 = row.toFloat() / TILES_PER_ROW
        val u1 = (col + 1).toFloat() / TILES_PER_ROW
        val v1 = (row + 1).toFloat() / TILES_PER_ROW
        return floatArrayOf(u0, v0, u1, v1)
    }

    fun initGL() {
        if (textureId != -1) {
            GLES20.glDeleteTextures(1, intArrayOf(textureId), 0)
        }

        val atlas = getOrCreateAtlasBitmap()
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        textureId = textures[0]

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, atlas, 0)
    }

    private fun generateAtlas(): Bitmap {
        val bitmap = Bitmap.createBitmap(ATLAS_SIZE, ATLAS_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply { isAntiAlias = false }

        fun px(c: Canvas, x: Int, y: Int, color: Int) {
            paint.color = color
            c.drawPoint(x.toFloat(), y.toFloat(), paint)
        }

        fun drawTile(tileIdx: Int, block: (Canvas, Int, Int) -> Unit) {
            val sx = (tileIdx % TILES_PER_ROW) * TILE_SIZE
            val sy = (tileIdx / TILES_PER_ROW) * TILE_SIZE
            block(canvas, sx, sy)
        }

        // --- Authentic Palette Constants ---
        val cGrassDark = 0xFF4A7424.toInt()
        val cGrassMid = 0xFF5C8E32.toInt()
        val cGrassLight = 0xFF6CA83C.toInt()
        val cGrassBright = 0xFF7CBD45.toInt()

        val cDirtDark = 0xFF5C3E29.toInt()
        val cDirtMid = 0xFF735137.toInt()
        val cDirtBase = 0xFF866043.toInt()
        val cDirtLight = 0xFF966C4A.toInt()
        val cDirtPebble = 0xFFAD825D.toInt()

        val cStoneDark = 0xFF585858.toInt()
        val cStoneMid = 0xFF6D6D6D.toInt()
        val cStoneBase = 0xFF7E7E7E.toInt()
        val cStoneLight = 0xFF8E8E8E.toInt()
        val cStoneFleck = 0xFF9E9E9E.toInt()

        // 0: Official Grass Top
        drawTile(0) { c, sx, sy ->
            val grassNoise = intArrayOf(
                1, 2, 0, 1, 3, 1, 2, 0, 1, 2, 3, 1, 0, 2, 1, 0,
                0, 1, 3, 2, 1, 0, 1, 3, 2, 1, 0, 2, 3, 1, 0, 1,
                2, 0, 1, 2, 0, 3, 2, 1, 0, 3, 1, 0, 1, 2, 3, 2,
                1, 3, 2, 0, 1, 2, 0, 1, 3, 2, 0, 1, 2, 0, 1, 3,
                0, 1, 0, 3, 2, 1, 3, 2, 1, 0, 2, 3, 1, 3, 2, 0,
                2, 3, 1, 2, 0, 2, 1, 0, 2, 3, 1, 0, 2, 1, 0, 1,
                1, 0, 2, 1, 3, 1, 3, 2, 1, 0, 3, 2, 1, 0, 3, 2,
                3, 2, 1, 0, 2, 0, 1, 0, 3, 2, 1, 0, 2, 3, 1, 0,
                1, 0, 3, 2, 1, 3, 2, 3, 2, 1, 0, 3, 1, 2, 0, 1,
                0, 2, 1, 0, 3, 2, 1, 0, 1, 3, 2, 1, 0, 1, 3, 2,
                2, 1, 0, 3, 2, 0, 3, 2, 0, 2, 0, 3, 2, 0, 1, 0,
                1, 3, 2, 1, 0, 1, 2, 1, 3, 1, 3, 1, 3, 2, 0, 3,
                0, 2, 0, 2, 3, 2, 0, 3, 2, 0, 2, 0, 1, 0, 3, 1,
                3, 1, 3, 1, 0, 1, 3, 1, 0, 1, 3, 2, 0, 2, 1, 2,
                1, 0, 2, 0, 2, 0, 2, 0, 2, 3, 1, 0, 3, 1, 0, 1,
                2, 3, 1, 3, 1, 3, 1, 3, 1, 0, 2, 3, 1, 2, 3, 0
            )
            val colors = intArrayOf(cGrassDark, cGrassMid, cGrassLight, cGrassBright)
            for (y in 0..15) {
                for (x in 0..15) {
                    px(c, sx + x, sy + y, colors[grassNoise[y * 16 + x]])
                }
            }
        }

        // 1: Official Grass Side (authentic drips + dirt)
        drawTile(1) { c, sx, sy ->
            val dripDepths = intArrayOf(3, 4, 3, 2, 3, 5, 4, 3, 2, 4, 3, 2, 4, 5, 3, 2)
            for (x in 0..15) {
                val drip = dripDepths[x]
                for (y in 0..15) {
                    val color = when {
                        y == 0 -> cGrassBright
                        y < drip - 1 -> cGrassLight
                        y == drip - 1 -> cGrassMid
                        y == drip -> cGrassDark
                        // Dirt base beneath drips
                        (x + y * 7) % 19 == 0 -> cDirtPebble
                        (x * 3 + y) % 11 == 0 -> cDirtDark
                        (x + y * 3) % 7 == 0 -> cDirtLight
                        (x * 2 + y * 5) % 13 == 0 -> cDirtMid
                        else -> cDirtBase
                    }
                    px(c, sx + x, sy + y, color)
                }
            }
        }

        // 2: Official Dirt
        drawTile(2) { c, sx, sy ->
            for (y in 0..15) {
                for (x in 0..15) {
                    val color = when {
                        (x == 3 && y == 3) || (x == 11 && y == 5) || (x == 6 && y == 11) || (x == 13 && y == 12) -> cDirtPebble
                        (x == 2 && y == 4) || (x == 12 && y == 5) || (x == 7 && y == 11) -> cDirtLight
                        (x * 5 + y * 3) % 11 == 0 -> cDirtDark
                        (x * 7 + y * 2) % 5 == 0 -> cDirtMid
                        (x + y * 3) % 4 == 0 -> cDirtLight
                        else -> cDirtBase
                    }
                    px(c, sx + x, sy + y, color)
                }
            }
        }

        // 3: Official Stone
        drawTile(3) { c, sx, sy ->
            for (y in 0..15) {
                for (x in 0..15) {
                    val color = when {
                        (x == 4 && y == 5) || (x == 12 && y == 3) || (x == 8 && y == 12) -> cStoneFleck
                        (x * 3 + y * 5) % 7 == 0 -> cStoneLight
                        (x * 7 + y * 3) % 11 == 0 -> cStoneDark
                        (x + y * 2) % 3 == 0 -> cStoneMid
                        else -> cStoneBase
                    }
                    px(c, sx + x, sy + y, color)
                }
            }
        }

        // 4: Official Cobblestone (rounded interlocking stones + dark mortar)
        drawTile(4) { c, sx, sy ->
            val cMortar = 0xFF383838.toInt()
            val cCobbleDark = 0xFF545454.toInt()
            val cCobbleMid = 0xFF6E6E6E.toInt()
            val cCobbleLight = 0xFF8A8A8A.toInt()
            val cCobbleHighlight = 0xFF9E9E9E.toInt()

            for (y in 0..15) {
                for (x in 0..15) {
                    val isMortar = (x == 0 && y in 2..8) || (x == 8 && (y in 0..5 || y in 9..15)) ||
                            (x == 15 && y in 4..12) || (y == 0 && x in 3..11) || (y == 7 && x in 0..9) ||
                            (y == 9 && x in 7..15) || (y == 15 && x in 1..8) ||
                            ((x == 4 && y == 3) || (x == 12 && y == 12))
                    val isHighlight = ((x in 1..3 && y == 1) || (x in 9..12 && y == 1) ||
                            (x in 1..4 && y == 8) || (x in 9..13 && y == 10))
                    val color = when {
                        isMortar -> cMortar
                        isHighlight -> cCobbleHighlight
                        (x + y) % 4 == 0 -> cCobbleLight
                        (x * 2 + y) % 3 == 0 -> cCobbleDark
                        else -> cCobbleMid
                    }
                    px(c, sx + x, sy + y, color)
                }
            }
        }

        // 5: Official Bedrock
        drawTile(5) { c, sx, sy ->
            val cBed0 = 0xFF141414.toInt()
            val cBed1 = 0xFF272727.toInt()
            val cBed2 = 0xFF454545.toInt()
            val cBed3 = 0xFF7A7A7A.toInt()
            for (y in 0..15) {
                for (x in 0..15) {
                    val color = when {
                        (x == 5 && y == 4) || (x == 11 && y == 10) || (x == 2 && y == 13) -> cBed3
                        (x * 7 + y * 13) % 5 == 0 -> cBed2
                        (x * 3 + y * 7) % 3 == 0 -> cBed1
                        else -> cBed0
                    }
                    px(c, sx + x, sy + y, color)
                }
            }
        }

        // 6: Official Oak Log Top
        drawTile(6) { c, sx, sy ->
            val cBarkDark = 0xFF382715.toInt()
            val cBarkLight = 0xFF543D22.toInt()
            val cWoodRings = 0xFF9E7C48.toInt()
            val cWoodCore = 0xFFB6935C.toInt()
            val cWoodDark = 0xFF836233.toInt()

            for (y in 0..15) {
                for (x in 0..15) {
                    val dx = x - 7.5f
                    val dy = y - 7.5f
                    val dist = Math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
                    val color = when {
                        dist >= 6.8f -> if ((x + y) % 2 == 0) cBarkDark else cBarkLight
                        dist >= 5.5f -> cWoodDark
                        dist >= 4.2f -> cWoodCore
                        dist >= 3.0f -> cWoodRings
                        dist >= 1.8f -> cWoodCore
                        else -> cWoodDark
                    }
                    px(c, sx + x, sy + y, color)
                }
            }
        }

        // 7: Official Oak Log Side
        drawTile(7) { c, sx, sy ->
            val cBarkDeep = 0xFF352414.toInt()
            val cBarkDark = 0xFF4E371F.toInt()
            val cBarkMid = 0xFF6D4E2C.toInt()
            val cBarkLight = 0xFF836038.toInt()

            for (x in 0..15) {
                val isGroove = (x == 1 || x == 5 || x == 10 || x == 14)
                for (y in 0..15) {
                    val color = when {
                        isGroove -> if ((y + x) % 3 == 0) cBarkDeep else cBarkDark
                        (x + 1 == 1 || x - 1 == 5 || x + 1 == 10) -> cBarkDark
                        (y % 4 == 0) -> cBarkLight
                        else -> cBarkMid
                    }
                    px(c, sx + x, sy + y, color)
                }
            }
        }

        // 8: Official Oak Planks (4 distinct horizontal planks)
        drawTile(8) { c, sx, sy ->
            val cPlankHighlight = 0xFFC29D64.toInt()
            val cPlankBody = 0xFFA57D46.toInt()
            val cPlankShadow = 0xFF8D6835.toInt()
            val cPlankSeam = 0xFF543C1D.toInt()
            val cNail = 0xFF3F2B14.toInt()

            for (y in 0..15) {
                val plankRow = y % 4
                for (x in 0..15) {
                    val isNail = (y == 1 && x == 2) || (y == 5 && x == 13) || (y == 9 && x == 3) || (y == 13 && x == 12)
                    val color = when {
                        isNail -> cNail
                        plankRow == 3 -> cPlankSeam
                        plankRow == 0 -> cPlankHighlight
                        (x * 3 + y) % 5 == 0 -> cPlankShadow
                        else -> cPlankBody
                    }
                    px(c, sx + x, sy + y, color)
                }
            }
        }

        // 9: Official Oak Leaves (semi-transparent leafy canopy)
        drawTile(9) { c, sx, sy ->
            val cLeafDark = 0xFF244813.toInt()
            val cLeafMid = 0xFF35681E.toInt()
            val cLeafLight = 0xFF49862B.toInt()
            val cLeafBright = 0xFF5CA337.toInt()

            for (y in 0..15) {
                for (x in 0..15) {
                    // Cutout holes
                    val isHole = (x + y * 5) % 11 == 0 || (x * 3 + y) % 17 == 0
                    if (isHole) {
                        px(c, sx + x, sy + y, Color.TRANSPARENT)
                    } else {
                        val color = when ((x * 7 + y * 13) % 4) {
                            0 -> cLeafDark
                            1 -> cLeafMid
                            2 -> cLeafLight
                            else -> cLeafBright
                        }
                        px(c, sx + x, sy + y, color)
                    }
                }
            }
        }

        // 10: Official Sand
        drawTile(10) { c, sx, sy ->
            val cSandBase = 0xFFD6CD92.toInt()
            val cSandLight = 0xFFE0D9A4.toInt()
            val cSandDark = 0xFFC5BA7C.toInt()
            val cSandShadow = 0xFFB4A66B.toInt()

            for (y in 0..15) {
                for (x in 0..15) {
                    val color = when {
                        (x * 5 + y * 7) % 11 == 0 -> cSandShadow
                        (x * 3 + y * 2) % 5 == 0 -> cSandDark
                        (x + y * 3) % 4 == 0 -> cSandLight
                        else -> cSandBase
                    }
                    px(c, sx + x, sy + y, color)
                }
            }
        }

        // 11: Official Water (translucent flowing blue)
        drawTile(11) { c, sx, sy ->
            val cWater1 = 0xC82B55B8.toInt()
            val cWater2 = 0xCE3866DB.toInt()
            val cWater3 = 0xC82247A0.toInt()
            val cWaterHighlight = 0xD44C78E8.toInt()

            for (y in 0..15) {
                for (x in 0..15) {
                    val wave = (x + y) % 4
                    val color = when (wave) {
                        0 -> cWaterHighlight
                        1 -> cWater2
                        2 -> cWater1
                        else -> cWater3
                    }
                    px(c, sx + x, sy + y, color)
                }
            }
        }

        // 12: Official Glass (clear center, solid border + double diagonal glare stripes)
        drawTile(12) { c, sx, sy ->
            val cFrame = 0xF0E8EFF8.toInt()
            val cGlare = 0xC0FFFFFF.toInt()
            val cGlassPane = 0x1A80B0D0.toInt()

            for (y in 0..15) {
                for (x in 0..15) {
                    val isBorder = (x == 0 || x == 15 || y == 0 || y == 15)
                    val isGlare1 = (x == y && x in 3..6) || (x == y - 1 && x in 4..5)
                    val isGlare2 = (x == y && x in 10..12)
                    val color = when {
                        isBorder -> cFrame
                        isGlare1 || isGlare2 -> cGlare
                        else -> cGlassPane
                    }
                    px(c, sx + x, sy + y, color)
                }
            }
        }

        // Helper for official Minecraft Ores (Coal, Iron, Gold, Diamond, Redstone)
        fun drawOfficialOre(tileIdx: Int, oreLight: Int, oreMid: Int, oreDark: Int) {
            drawTile(tileIdx) { c, sx, sy ->
                // Stone base
                for (y in 0..15) {
                    for (x in 0..15) {
                        val stoneCol = when {
                            (x * 3 + y * 5) % 7 == 0 -> cStoneLight
                            (x * 7 + y * 3) % 11 == 0 -> cStoneDark
                            else -> cStoneBase
                        }
                        px(c, sx + x, sy + y, stoneCol)
                    }
                }
                // Signature ore flecks
                val oreClusters = listOf(
                    // Cluster 1 (Top Left)
                    Triple(3, 3, oreDark), Triple(4, 3, oreMid), Triple(3, 4, oreLight), Triple(4, 4, oreMid),
                    // Cluster 2 (Top Right)
                    Triple(10, 2, oreDark), Triple(11, 2, oreLight), Triple(11, 3, oreMid), Triple(12, 3, oreDark),
                    // Cluster 3 (Center)
                    Triple(6, 8, oreDark), Triple(7, 8, oreLight), Triple(8, 8, oreLight), Triple(7, 9, oreMid), Triple(8, 9, oreDark),
                    // Cluster 4 (Bottom Right)
                    Triple(12, 10, oreMid), Triple(13, 10, oreLight), Triple(12, 11, oreDark), Triple(13, 11, oreMid),
                    // Cluster 5 (Bottom Left)
                    Triple(2, 11, oreDark), Triple(3, 11, oreLight), Triple(3, 12, oreMid), Triple(4, 12, oreDark)
                )
                for ((ox, oy, oCol) in oreClusters) {
                    px(c, sx + ox, sy + oy, oCol)
                }
            }
        }

        drawOfficialOre(13, 0xFF3D3D3D.toInt(), 0xFF222222.toInt(), 0xFF111111.toInt()) // Coal Ore
        drawOfficialOre(14, 0xFFE0BCA4.toInt(), 0xFFD1A688.toInt(), 0xFFB38668.toInt()) // Iron Ore
        drawOfficialOre(15, 0xFFFFF15C.toInt(), 0xFFFCE12D.toInt(), 0xFFC6A212.toInt()) // Gold Ore
        drawOfficialOre(16, 0xFFB4FCF7.toInt(), 0xFF5DF8EB.toInt(), 0xFF19B2A6.toInt()) // Diamond Ore
        drawOfficialOre(17, 0xFFFF5C5C.toInt(), 0xFFE61919.toInt(), 0xFF9E0B0B.toInt()) // Redstone Ore

        // 18: Official Crafting Table Top (3x3 grid & tools)
        drawTile(18) { c, sx, sy ->
            val cGridWood = 0xFFC89D66.toInt()
            val cGridLine = 0xFF7D582A.toInt()
            val cRim = 0xFF523919.toInt()

            for (y in 0..15) {
                for (x in 0..15) {
                    val isBorder = x == 0 || x == 15 || y == 0 || y == 15
                    val isGrid = (x == 5 || x == 10 || y == 5 || y == 10)
                    val color = when {
                        isBorder -> cRim
                        isGrid -> cGridLine
                        else -> cGridWood
                    }
                    px(c, sx + x, sy + y, color)
                }
            }
            // Crossed tool details in center
            px(c, sx + 7, sy + 7, 0xFF454545.toInt())
            px(c, sx + 8, sy + 8, 0xFF454545.toInt())
            px(c, sx + 7, sy + 8, 0xFF8F632B.toInt())
            px(c, sx + 8, sy + 7, 0xFF8F632B.toInt())
        }

        // 19: Official Crafting Table Side
        drawTile(19) { c, sx, sy ->
            for (y in 0..15) {
                for (x in 0..15) {
                    val isFrame = x in 1..14 && (y == 1 || y == 14 || x == 1 || x == 14)
                    val isWood = x in 2..13 && y in 2..13
                    val color = when {
                        isFrame -> 0xFF543C1D.toInt()
                        isWood -> 0xFFA57D46.toInt()
                        else -> 0xFF6B4D26.toInt()
                    }
                    px(c, sx + x, sy + y, color)
                }
            }
            // Hanging saw / tool
            for (i in 4..11) {
                px(c, sx + 4, sy + i, 0xFFB8B8B8.toInt())
                px(c, sx + 5, sy + i, if (i % 2 == 0) 0xFF8E8E8E.toInt() else 0xFFD0D0D0.toInt())
            }
            px(c, sx + 4, sy + 3, 0xFF63411B.toInt())
            px(c, sx + 5, sy + 3, 0xFF63411B.toInt())
        }

        // 20: Official Furnace Top
        drawTile(20) { c, sx, sy ->
            for (y in 0..15) {
                for (x in 0..15) {
                    val color = when {
                        (x == 0 || x == 15 || y == 0 || y == 15) -> 0xFF505050.toInt()
                        (x * 5 + y * 7) % 6 == 0 -> 0xFF686868.toInt()
                        else -> 0xFF7A7A7A.toInt()
                    }
                    px(c, sx + x, sy + y, color)
                }
            }
        }

        // 21: Official Furnace Front (stone frame + glowing fire hearth)
        drawTile(21) { c, sx, sy ->
            for (y in 0..15) {
                for (x in 0..15) {
                    val isHearth = (x in 3..12 && y in 8..13)
                    val isArch = (x in 4..11 && y == 7)
                    val color = when {
                        isHearth || isArch -> 0xFF1A1A1A.toInt()
                        (x == 0 || x == 15 || y == 0 || y == 15) -> 0xFF4E4E4E.toInt()
                        (x + y * 2) % 3 == 0 -> 0xFF666666.toInt()
                        else -> 0xFF767676.toInt()
                    }
                    px(c, sx + x, sy + y, color)
                }
            }
            // Burning fire embers
            val embers = listOf(
                Triple(6, 11, 0xFFFF4500.toInt()), Triple(7, 11, 0xFFFF9500.toInt()), Triple(8, 11, 0xFFFFEA00.toInt()),
                Triple(7, 10, 0xFFFF6A00.toInt()), Triple(8, 10, 0xFFFFD000.toInt()), Triple(9, 11, 0xFFFF5000.toInt()),
                Triple(7, 12, 0xFFFF3700.toInt()), Triple(8, 12, 0xFFFF8000.toInt())
            )
            for ((ex, ey, eCol) in embers) {
                px(c, sx + ex, sy + ey, eCol)
            }
        }

        // 22: Official TNT Top
        drawTile(22) { c, sx, sy ->
            val cRed = 0xFFD82828.toInt()
            val cWhite = 0xFFEEEEEE.toInt()
            val cFuse = 0xFF2A2A2A.toInt()

            for (y in 0..15) {
                for (x in 0..15) {
                    val dx = x - 7.5f
                    val dy = y - 7.5f
                    val dist = Math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
                    val color = when {
                        dist <= 1.2f -> cFuse
                        dist <= 4.0f -> cWhite
                        else -> cRed
                    }
                    px(c, sx + x, sy + y, color)
                }
            }
        }

        // 23: Official TNT Bottom
        drawTile(23) { c, sx, sy ->
            for (y in 0..15) {
                for (x in 0..15) {
                    px(c, sx + x, sy + y, if ((x + y) % 3 == 0) 0xFFC02020.toInt() else 0xFFD82828.toInt())
                }
            }
        }

        // 24: Official TNT Side (red sticks + crisp "TNT" label)
        drawTile(24) { c, sx, sy ->
            val cRed = 0xFFD82828.toInt()
            val cRedDark = 0xFFB81818.toInt()
            val cWhite = 0xFFF2F2F2.toInt()
            val cBlack = 0xFF141414.toInt()

            for (y in 0..15) {
                val isLabel = y in 6..9
                for (x in 0..15) {
                    val color = when {
                        isLabel -> cWhite
                        x % 2 == 0 -> cRedDark
                        else -> cRed
                    }
                    px(c, sx + x, sy + y, color)
                }
            }
            // Pixel-perfect "TNT" letters:
            // T:
            px(c, sx + 2, sy + 7, cBlack); px(c, sx + 3, sy + 7, cBlack); px(c, sx + 4, sy + 7, cBlack)
            px(c, sx + 3, sy + 8, cBlack); px(c, sx + 3, sy + 9, cBlack)
            // N:
            px(c, sx + 6, sy + 7, cBlack); px(c, sx + 6, sy + 8, cBlack); px(c, sx + 6, sy + 9, cBlack)
            px(c, sx + 7, sy + 8, cBlack)
            px(c, sx + 8, sy + 7, cBlack); px(c, sx + 8, sy + 8, cBlack); px(c, sx + 8, sy + 9, cBlack)
            // T:
            px(c, sx + 10, sy + 7, cBlack); px(c, sx + 11, sy + 7, cBlack); px(c, sx + 12, sy + 7, cBlack)
            px(c, sx + 11, sy + 8, cBlack); px(c, sx + 11, sy + 9, cBlack)
        }

        // 25: Official Bricks (terracotta running bond with mortar)
        drawTile(25) { c, sx, sy ->
            val cMortar = 0xFFD2C7B8.toInt()
            val cBrickBase = 0xFF993B2B.toInt()
            val cBrickLight = 0xFFB24937.toInt()
            val cBrickDark = 0xFF7D2C1F.toInt()

            for (y in 0..15) {
                val isHSeam = (y == 3 || y == 7 || y == 11 || y == 15)
                val row = y / 4
                for (x in 0..15) {
                    val isVSeam = when (row) {
                        0, 2 -> (x == 4 || x == 12)
                        else -> (x == 0 || x == 8)
                    }
                    val color = when {
                        isHSeam || isVSeam -> cMortar
                        (x + y) % 3 == 0 -> cBrickLight
                        (x * 3 + y) % 5 == 0 -> cBrickDark
                        else -> cBrickBase
                    }
                    px(c, sx + x, sy + y, color)
                }
            }
        }

        // 26: Official Bookshelf
        drawTile(26) { c, sx, sy ->
            val cShelf = 0xFFA57D46.toInt()
            val cShelfShadow = 0xFF543C1D.toInt()
            for (y in 0..15) {
                val isWood = (y == 0 || y == 7 || y == 8 || y == 15)
                for (x in 0..15) {
                    px(c, sx + x, sy + y, if (isWood) cShelf else cShelfShadow)
                }
            }
            // Books
            val bookCols = intArrayOf(
                0xFFB82828.toInt(), 0xFF2854B8.toInt(), 0xFF288B28.toInt(), 0xFFD89C28.toInt(), 0xFF7B28B8.toInt(),
                0xFFB82828.toInt(), 0xFF288B8B.toInt(), 0xFF8B2828.toInt()
            )
            for (b in 0..6) {
                val bx = 1 + b * 2
                val col = bookCols[b % bookCols.size]
                for (by in 1..6) {
                    px(c, sx + bx, sy + by, col)
                    px(c, sx + bx + 1, sy + by, if (by == 3) 0xFFF0E0B0.toInt() else col)
                }
                for (by in 9..14) {
                    val col2 = bookCols[(b + 3) % bookCols.size]
                    px(c, sx + bx, sy + by, col2)
                    px(c, sx + bx + 1, sy + by, if (by == 12) 0xFFF0E0B0.toInt() else col2)
                }
            }
        }

        // 27: Official Torch
        drawTile(27) { c, sx, sy ->
            // Transparent background
            for (y in 0..15) {
                for (x in 0..15) {
                    px(c, sx + x, sy + y, Color.TRANSPARENT)
                }
            }
            // Stick shaft
            for (y in 6..14) {
                px(c, sx + 7, sy + y, 0xFF885E32.toInt())
                px(c, sx + 8, sy + y, 0xFF684520.toInt())
            }
            // Charcoal head
            px(c, sx + 7, sy + 5, 0xFF382512.toInt())
            px(c, sx + 8, sy + 5, 0xFF28180A.toInt())
            // Flame
            px(c, sx + 7, sy + 2, 0xFFFFDD33.toInt())
            px(c, sx + 8, sy + 2, 0xFFFFDD33.toInt())
            px(c, sx + 6, sy + 3, 0xFFFF9900.toInt())
            px(c, sx + 7, sy + 3, 0xFFFFFFEE.toInt())
            px(c, sx + 8, sy + 3, 0xFFFFFFEE.toInt())
            px(c, sx + 9, sy + 3, 0xFFFF9900.toInt())
            px(c, sx + 7, sy + 4, 0xFFFF5500.toInt())
            px(c, sx + 8, sy + 4, 0xFFFF5500.toInt())
        }

        // 28: Official Poppy
        drawTile(28) { c, sx, sy ->
            for (y in 0..15) {
                for (x in 0..15) {
                    px(c, sx + x, sy + y, Color.TRANSPARENT)
                }
            }
            // Stem & leaf
            for (y in 7..14) px(c, sx + 8, sy + y, 0xFF358525.toInt())
            px(c, sx + 7, sy + 11, 0xFF358525.toInt())
            px(c, sx + 9, sy + 9, 0xFF358525.toInt())
            // Red Poppy blossom
            val petals = listOf(
                Triple(7, 3, 0xFFE02020.toInt()), Triple(8, 3, 0xFFE02020.toInt()),
                Triple(6, 4, 0xFFC01515.toInt()), Triple(7, 4, 0xFFFA3535.toInt()), Triple(8, 4, 0xFF300808.toInt()), Triple(9, 4, 0xFFC01515.toInt()),
                Triple(6, 5, 0xFFC01515.toInt()), Triple(7, 5, 0xFFFA3535.toInt()), Triple(8, 5, 0xFFFA3535.toInt()), Triple(9, 5, 0xFFC01515.toInt()),
                Triple(7, 6, 0xFFC01515.toInt()), Triple(8, 6, 0xFFC01515.toInt())
            )
            for ((pxPos, pyPos, col) in petals) {
                px(c, sx + pxPos, sy + pyPos, col)
            }
        }

        // 29: Official Dandelion
        drawTile(29) { c, sx, sy ->
            for (y in 0..15) {
                for (x in 0..15) {
                    px(c, sx + x, sy + y, Color.TRANSPARENT)
                }
            }
            for (y in 6..14) px(c, sx + 8, sy + y, 0xFF358525.toInt())
            px(c, sx + 7, sy + 10, 0xFF358525.toInt())
            // Yellow Flower Head
            val yellowFlower = listOf(
                Triple(7, 2, 0xFFFFDD00.toInt()), Triple(8, 2, 0xFFFFDD00.toInt()),
                Triple(6, 3, 0xFFFFC400.toInt()), Triple(7, 3, 0xFFFFF266.toInt()), Triple(8, 3, 0xFFFFF266.toInt()), Triple(9, 3, 0xFFFFC400.toInt()),
                Triple(6, 4, 0xFFFFC400.toInt()), Triple(7, 4, 0xFFFFAA00.toInt()), Triple(8, 4, 0xFFFFAA00.toInt()), Triple(9, 4, 0xFFFFC400.toInt()),
                Triple(7, 5, 0xFFFFC400.toInt()), Triple(8, 5, 0xFFFFC400.toInt())
            )
            for ((fx, fy, col) in yellowFlower) {
                px(c, sx + fx, sy + fy, col)
            }
        }

        // Official Tools & Weapons Helper (45 degree angle pixel art)
        fun drawOfficialSword(tileIdx: Int, cBladeLight: Int, cBladeMid: Int, cBladeDark: Int) {
            drawTile(tileIdx) { c, sx, sy ->
                for (y in 0..15) for (x in 0..15) px(c, sx + x, sy + y, Color.TRANSPARENT)
                // Handle
                px(c, sx + 2, sy + 13, 0xFF5C3E20.toInt())
                px(c, sx + 3, sy + 12, 0xFF7D542C.toInt())
                px(c, sx + 4, sy + 11, 0xFF5C3E20.toInt())
                // Guard
                px(c, sx + 3, sy + 10, 0xFF3C3C3C.toInt())
                px(c, sx + 4, sy + 10, 0xFF5C5C5C.toInt())
                px(c, sx + 5, sy + 10, 0xFF7D542C.toInt())
                px(c, sx + 5, sy + 11, 0xFF5C5C5C.toInt())
                px(c, sx + 5, sy + 12, 0xFF3C3C3C.toInt())
                // Double-edged blade
                for (i in 0..6) {
                    val bx = 5 + i
                    val by = 9 - i
                    px(c, sx + bx, sy + by, cBladeLight)
                    px(c, sx + bx + 1, sy + by, cBladeMid)
                    px(c, sx + bx + 1, sy + by + 1, cBladeDark)
                }
                // Tip
                px(c, sx + 12, sy + 2, cBladeLight)
                px(c, sx + 13, sy + 2, cBladeMid)
            }
        }

        fun drawOfficialPickaxe(tileIdx: Int, cHeadLight: Int, cHeadMid: Int, cHeadDark: Int) {
            drawTile(tileIdx) { c, sx, sy ->
                for (y in 0..15) for (x in 0..15) px(c, sx + x, sy + y, Color.TRANSPARENT)
                // Wooden stick handle
                for (i in 0..8) {
                    px(c, sx + 3 + i, sy + 12 - i, 0xFF7D542C.toInt())
                }
                // Pickaxe arch head
                val head = listOf(
                    Triple(8, 2, cHeadLight), Triple(9, 2, cHeadLight), Triple(10, 2, cHeadLight), Triple(11, 2, cHeadLight),
                    Triple(7, 3, cHeadMid), Triple(12, 3, cHeadMid),
                    Triple(6, 4, cHeadDark), Triple(13, 4, cHeadDark),
                    Triple(5, 5, cHeadDark), Triple(14, 5, cHeadDark)
                )
                for ((hx, hy, col) in head) {
                    px(c, sx + hx, sy + hy, col)
                }
            }
        }

        // Tools (30..37)
        drawOfficialPickaxe(30, 0xFFC49D66.toInt(), 0xFFA57D46.toInt(), 0xFF7D582A.toInt()) // Wood
        drawOfficialPickaxe(31, 0xFF9E9E9E.toInt(), 0xFF7E7E7E.toInt(), 0xFF585858.toInt()) // Stone
        drawOfficialPickaxe(32, 0xFFFFFFFF.toInt(), 0xFFD8D8DC.toInt(), 0xFFA4A4AC.toInt()) // Iron
        drawOfficialPickaxe(33, 0xFFB4FCF7.toInt(), 0xFF5DF8EB.toInt(), 0xFF19B2A6.toInt()) // Diamond

        drawOfficialSword(34, 0xFFC49D66.toInt(), 0xFFA57D46.toInt(), 0xFF7D582A.toInt())
        drawOfficialSword(35, 0xFF9E9E9E.toInt(), 0xFF7E7E7E.toInt(), 0xFF585858.toInt())
        drawOfficialSword(36, 0xFFFFFFFF.toInt(), 0xFFD8D8DC.toInt(), 0xFFA4A4AC.toInt())
        drawOfficialSword(37, 0xFFB4FCF7.toInt(), 0xFF5DF8EB.toInt(), 0xFF19B2A6.toInt())

        // 38: Stick
        drawTile(38) { c, sx, sy ->
            for (y in 0..15) for (x in 0..15) px(c, sx + x, sy + y, Color.TRANSPARENT)
            for (i in 0..11) {
                px(c, sx + 2 + i, sy + 13 - i, 0xFF7D542C.toInt())
                px(c, sx + 3 + i, sy + 13 - i, 0xFF54391D.toInt())
            }
        }

        // 39: Coal Lump
        drawTile(39) { c, sx, sy ->
            for (y in 0..15) for (x in 0..15) px(c, sx + x, sy + y, Color.TRANSPARENT)
            val coal = listOf(
                Triple(6, 4, 0xFF353535.toInt()), Triple(7, 4, 0xFF4A4A4A.toInt()), Triple(8, 4, 0xFF222222.toInt()),
                Triple(5, 5, 0xFF4A4A4A.toInt()), Triple(6, 5, 0xFF656565.toInt()), Triple(7, 5, 0xFF353535.toInt()), Triple(8, 5, 0xFF222222.toInt()), Triple(9, 5, 0xFF141414.toInt()),
                Triple(4, 6, 0xFF353535.toInt()), Triple(5, 6, 0xFF353535.toInt()), Triple(6, 6, 0xFF222222.toInt()), Triple(7, 6, 0xFF141414.toInt()), Triple(8, 6, 0xFF141414.toInt()),
                Triple(4, 7, 0xFF222222.toInt()), Triple(5, 7, 0xFF141414.toInt()), Triple(6, 7, 0xFF141414.toInt()), Triple(7, 7, 0xFF141414.toInt())
            )
            for ((cx, cy, col) in coal) px(c, sx + cx, sy + cy, col)
        }

        // 40: Iron Ingot (isometric metallic sheen)
        drawTile(40) { c, sx, sy ->
            for (y in 0..15) for (x in 0..15) px(c, sx + x, sy + y, Color.TRANSPARENT)
            for (y in 6..10) {
                for (x in 4..12) {
                    val col = when {
                        y == 6 -> 0xFFFFFFFF.toInt()
                        y == 7 -> 0xFFE0E0E6.toInt()
                        x == 4 || y == 10 -> 0xFF8E8E96.toInt()
                        else -> 0xFFC0C0C8.toInt()
                    }
                    px(c, sx + x, sy + y, col)
                }
            }
        }

        // 41: Gold Ingot
        drawTile(41) { c, sx, sy ->
            for (y in 0..15) for (x in 0..15) px(c, sx + x, sy + y, Color.TRANSPARENT)
            for (y in 6..10) {
                for (x in 4..12) {
                    val col = when {
                        y == 6 -> 0xFFFFF880.toInt()
                        y == 7 -> 0xFFFEE630.toInt()
                        x == 4 || y == 10 -> 0xFFB58E08.toInt()
                        else -> 0xFFE5BC14.toInt()
                    }
                    px(c, sx + x, sy + y, col)
                }
            }
        }

        // 42: Diamond Gem (iconic multifaceted brilliant cut)
        drawTile(42) { c, sx, sy ->
            for (y in 0..15) for (x in 0..15) px(c, sx + x, sy + y, Color.TRANSPARENT)
            val gem = listOf(
                Triple(7, 3, 0xFFE0FFFF.toInt()), Triple(8, 3, 0xFFE0FFFF.toInt()),
                Triple(6, 4, 0xFFE0FFFF.toInt()), Triple(7, 4, 0xFFB4FCF7.toInt()), Triple(8, 4, 0xFF5DF8EB.toInt()), Triple(9, 4, 0xFF28D0C0.toInt()),
                Triple(5, 5, 0xFFB4FCF7.toInt()), Triple(6, 5, 0xFF5DF8EB.toInt()), Triple(7, 5, 0xFF5DF8EB.toInt()), Triple(8, 5, 0xFF28D0C0.toInt()), Triple(9, 5, 0xFF18A090.toInt()),
                Triple(6, 6, 0xFF5DF8EB.toInt()), Triple(7, 6, 0xFF28D0C0.toInt()), Triple(8, 6, 0xFF18A090.toInt()),
                Triple(7, 7, 0xFF18A090.toInt())
            )
            for ((gx, gy, col) in gem) px(c, sx + gx, sy + gy, col)
        }

        // 43: Apple
        drawTile(43) { c, sx, sy ->
            for (y in 0..15) for (x in 0..15) px(c, sx + x, sy + y, Color.TRANSPARENT)
            px(c, sx + 8, sy + 2, 0xFF6B4218.toInt())
            px(c, sx + 9, sy + 3, 0xFF4A8820.toInt())
            for (y in 4..11) {
                for (x in 5..11) {
                    val col = when {
                        x == 6 && y == 5 -> 0xFFFF8888.toInt()
                        (x == 5 && (y == 4 || y == 11)) || (x == 11 && (y == 4 || y == 11)) -> Color.TRANSPARENT
                        y == 4 || y == 11 -> 0xFFC01515.toInt()
                        else -> 0xFFE82525.toInt()
                    }
                    if (col != Color.TRANSPARENT) px(c, sx + x, sy + y, col)
                }
            }
        }

        // 44: Bread
        drawTile(44) { c, sx, sy ->
            for (y in 0..15) for (x in 0..15) px(c, sx + x, sy + y, Color.TRANSPARENT)
            for (y in 6..10) {
                for (x in 3..12) {
                    val col = when {
                        y == 6 -> 0xFFE2A04A.toInt()
                        x in 5..6 || x in 9..10 -> 0xFF9E5C20.toInt()
                        else -> 0xFFC97E2C.toInt()
                    }
                    px(c, sx + x, sy + y, col)
                }
            }
        }

        // 45: Cooked Porkchop
        drawTile(45) { c, sx, sy ->
            for (y in 0..15) for (x in 0..15) px(c, sx + x, sy + y, Color.TRANSPARENT)
            px(c, sx + 4, sy + 8, 0xFFF0EAE0.toInt())
            px(c, sx + 4, sy + 9, 0xFFD8D0C0.toInt())
            for (y in 5..10) {
                for (x in 5..12) {
                    val col = when {
                        y == 5 || x == 12 -> 0xFF8A3C20.toInt()
                        (x + y) % 3 == 0 -> 0xFFC66740.toInt()
                        else -> 0xFFA84E2C.toInt()
                    }
                    px(c, sx + x, sy + y, col)
                }
            }
        }

        // 46: Official Sandstone Top
        drawTile(46) { c, sx, sy ->
            for (y in 0..15) {
                for (x in 0..15) {
                    val col = if ((x + y) % 3 == 0) 0xFFDCD29E.toInt() else 0xFFD5CA92.toInt()
                    px(c, sx + x, sy + y, col)
                }
            }
        }

        // 47: Official Sandstone Side (strata + carved glyph border)
        drawTile(47) { c, sx, sy ->
            for (y in 0..15) {
                for (x in 0..15) {
                    val col = when {
                        y in 0..3 -> 0xFFDCD29E.toInt()
                        y == 4 || y == 11 -> 0xFFB4A56C.toInt()
                        y in 5..10 -> if ((x + y) % 4 == 0) 0xFFBFB075.toInt() else 0xFFC8B97E.toInt()
                        else -> 0xFFD5CA92.toInt()
                    }
                    px(c, sx + x, sy + y, col)
                }
            }
        }

        // 60: Official Snow Block
        drawTile(60) { c, sx, sy ->
            for (y in 0..15) {
                for (x in 0..15) {
                    val col = when {
                        (x + y * 5) % 13 == 0 -> 0xFFE0EBF5.toInt()
                        (x * 3 + y) % 7 == 0 -> 0xFFEEF5FA.toInt()
                        else -> 0xFFFFFFFF.toInt()
                    }
                    px(c, sx + x, sy + y, col)
                }
            }
        }

        // 61: Official Birch Log Side (white bark with black marks)
        drawTile(61) { c, sx, sy ->
            for (y in 0..15) {
                for (x in 0..15) {
                    val isMark = (y in 3..4 && x in 3..6) || (y in 10..11 && x in 9..13) || (y in 7..8 && x in 0..2)
                    val col = when {
                        isMark -> 0xFF2B2B2B.toInt()
                        (x + y) % 5 == 0 -> 0xFFE4E6DE.toInt()
                        else -> 0xFFF2F4EC.toInt()
                    }
                    px(c, sx + x, sy + y, col)
                }
            }
        }

        // 62: Official Birch Leaves (lime foliage)
        drawTile(62) { c, sx, sy ->
            for (y in 0..15) {
                for (x in 0..15) {
                    if ((x + y * 3) % 9 == 0) {
                        px(c, sx + x, sy + y, Color.TRANSPARENT)
                    } else {
                        val col = when ((x * 5 + y * 7) % 3) {
                            0 -> 0xFF588826.toInt()
                            1 -> 0xFF6DA434.toInt()
                            else -> 0xFF80BC42.toInt()
                        }
                        px(c, sx + x, sy + y, col)
                    }
                }
            }
        }

        // 63: Shield
        drawTile(63) { c, sx, sy ->
            for (y in 0..15) for (x in 0..15) px(c, sx + x, sy + y, Color.TRANSPARENT)
            for (y in 2..13) {
                for (x in 4..11) {
                    val isBorder = x == 4 || x == 11 || y == 2 || y == 13
                    val isBoss = x in 7..8 && y in 7..8
                    val col = when {
                        isBorder || isBoss -> 0xFFC0C0C6.toInt()
                        else -> 0xFFA57D46.toInt()
                    }
                    px(c, sx + x, sy + y, col)
                }
            }
        }

        // 64: Copper Block (1.21 Tricky Trials copper plating)
        drawTile(64) { c, sx, sy ->
            for (y in 0..15) {
                for (x in 0..15) {
                    val isSeam = x == 0 || x == 15 || y == 0 || y == 15 || x == 8 || y == 8
                    val col = when {
                        isSeam -> 0xFF9E4832.toInt()
                        (x in 1..2 || y in 1..2) -> 0xFFDE765A.toInt()
                        else -> 0xFFC15C42.toInt()
                    }
                    px(c, sx + x, sy + y, col)
                }
            }
        }

        // 65: Crafter Top (1.21 signature auto-crafter)
        drawTile(65) { c, sx, sy ->
            for (y in 0..15) {
                for (x in 0..15) {
                    val isBorder = x == 0 || x == 15 || y == 0 || y == 15
                    val isRedstone = x in 6..9 && y in 6..9
                    val col = when {
                        isBorder -> 0xFFC15C42.toInt() // Copper edge
                        isRedstone -> 0xFFFF2020.toInt()
                        else -> 0xFF5C5C64.toInt()
                    }
                    px(c, sx + x, sy + y, col)
                }
            }
        }

        // 66: Crafter Side
        drawTile(66) { c, sx, sy ->
            for (y in 0..15) {
                for (x in 0..15) {
                    val isMouth = y in 11..13 && x in 5..10
                    val isGrid = y in 3..9 && x in 3..12 && (x % 3 == 0 || y % 3 == 0)
                    val col = when {
                        isMouth -> 0xFF1E1E22.toInt()
                        isGrid -> 0xFF36363C.toInt()
                        (x == 0 || x == 15) -> 0xFFC15C42.toInt()
                        else -> 0xFF606068.toInt()
                    }
                    px(c, sx + x, sy + y, col)
                }
            }
        }

        // 67: Cherry Planks (1.20 blossom pink wood)
        drawTile(67) { c, sx, sy ->
            for (y in 0..15) {
                val row = y % 4
                for (x in 0..15) {
                    val col = when (row) {
                        3 -> 0xFFB87082.toInt()
                        0 -> 0xFFF6BAC5.toInt()
                        else -> 0xFFEAA6B3.toInt()
                    }
                    px(c, sx + x, sy + y, col)
                }
            }
        }

        // 68: Tuff (1.21 volcanic stone)
        drawTile(68) { c, sx, sy ->
            for (y in 0..15) {
                for (x in 0..15) {
                    val col = when {
                        (x * 3 + y * 7) % 5 == 0 -> 0xFF4A4E4E.toInt()
                        (x + y) % 3 == 0 -> 0xFF353939.toInt()
                        else -> 0xFF3F4444.toInt()
                    }
                    px(c, sx + x, sy + y, col)
                }
            }
        }

        // 69: Copper Ingot
        drawTile(69) { c, sx, sy ->
            for (y in 0..15) for (x in 0..15) px(c, sx + x, sy + y, Color.TRANSPARENT)
            for (y in 6..10) {
                for (x in 4..12) {
                    val col = when {
                        y == 6 -> 0xFFFF9E82.toInt()
                        y == 7 -> 0xFFDE765A.toInt()
                        x == 4 || y == 10 -> 0xFF9E4832.toInt()
                        else -> 0xFFC15C42.toInt()
                    }
                    px(c, sx + x, sy + y, col)
                }
            }
        }

        // 70: Wind Charge (1.21 projectile)
        drawTile(70) { c, sx, sy ->
            for (y in 0..15) for (x in 0..15) px(c, sx + x, sy + y, Color.TRANSPARENT)
            for (y in 3..12) {
                for (x in 3..12) {
                    val dx = x - 7.5f
                    val dy = y - 7.5f
                    val dist = Math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
                    if (dist <= 4.5f) {
                        val col = when {
                            dist <= 2.0f -> 0xFFFFFFFF.toInt()
                            (x + y) % 2 == 0 -> 0xFFD8F2F8.toInt()
                            else -> 0xFF98DCF0.toInt()
                        }
                        px(c, sx + x, sy + y, col)
                    }
                }
            }
        }

        // 71: Mace (1.21 heavy weapon)
        drawTile(71) { c, sx, sy ->
            for (y in 0..15) for (x in 0..15) px(c, sx + x, sy + y, Color.TRANSPARENT)
            // Breeze rod shaft
            for (i in 0..7) px(c, sx + 2 + i, sy + 13 - i, 0xFFE0D8A0.toInt())
            // Heavy Core head
            for (y in 2..7) {
                for (x in 8..13) {
                    val col = when {
                        x == 8 || y == 2 -> 0xFF949AA4.toInt()
                        x == 13 || y == 7 -> 0xFF40444C.toInt()
                        else -> 0xFF656A74.toInt()
                    }
                    px(c, sx + x, sy + y, col)
                }
            }
        }

        // 72: Netherite Ingot
        drawTile(72) { c, sx, sy ->
            for (y in 0..15) for (x in 0..15) px(c, sx + x, sy + y, Color.TRANSPARENT)
            for (y in 6..10) {
                for (x in 4..12) {
                    val col = when {
                        y == 6 -> 0xFF6B6266.toInt()
                        y == 7 -> 0xFF524A4E.toInt()
                        x == 4 || y == 10 -> 0xFF2D272A.toInt()
                        else -> 0xFF40393D.toInt()
                    }
                    px(c, sx + x, sy + y, col)
                }
            }
        }

        // 73: Netherite Sword
        drawOfficialSword(73, 0xFF6B6266.toInt(), 0xFF524A4E.toInt(), 0xFF352F32.toInt())

        // 74: Netherite Pickaxe
        drawOfficialPickaxe(74, 0xFF6B6266.toInt(), 0xFF524A4E.toInt(), 0xFF352F32.toInt())

        // 75: Sulfur Ore (Stone with vibrant sulfur yellow crystalline flecks)
        drawTile(75) { c, sx, sy ->
            // Base stone
            val stoneRnd = Random(75001L)
            for (y in 0..15) {
                for (x in 0..15) {
                    val shade = 105 + stoneRnd.nextInt(35)
                    px(c, sx + x, sy + y, Color.rgb(shade, shade, shade))
                }
            }
            // Sulfur flecks (bright yellow / golden)
            val sulfurFlecks = listOf(
                Pair(3, 4), Pair(4, 4), Pair(4, 5), Pair(3, 5),
                Pair(9, 2), Pair(10, 2), Pair(10, 3),
                Pair(7, 8), Pair(8, 8), Pair(8, 9), Pair(7, 9), Pair(9, 9),
                Pair(2, 11), Pair(3, 11), Pair(3, 12),
                Pair(11, 10), Pair(12, 10), Pair(12, 11), Pair(13, 11),
                Pair(6, 13), Pair(7, 13)
            )
            for ((fx, fy) in sulfurFlecks) {
                val col = if ((fx + fy) % 2 == 0) 0xFFFFEE00.toInt() else 0xFFFFD700.toInt()
                px(c, sx + fx, sy + fy, col)
            }
        }

        // 76: Sulfur Block (Crystalline golden-yellow mineral block)
        drawTile(76) { c, sx, sy ->
            val rnd = Random(76002L)
            for (y in 0..15) {
                for (x in 0..15) {
                    val base = 210 + rnd.nextInt(45)
                    val red = (base * 1.0f).toInt().coerceIn(0, 255)
                    val green = (base * 0.88f).toInt().coerceIn(0, 255)
                    val blue = (20 + rnd.nextInt(30)).coerceIn(0, 255)
                    val border = (x == 0 || y == 0 || x == 15 || y == 15)
                    val finalCol = if (border) {
                        Color.rgb((red * 0.8f).toInt(), (green * 0.8f).toInt(), blue)
                    } else {
                        Color.rgb(red, green, blue)
                    }
                    px(c, sx + x, sy + y, finalCol)
                }
            }
        }

        // 77: Sulfur Cube (High-energy glowing sulfur cube)
        drawTile(77) { c, sx, sy ->
            val rnd = Random(77003L)
            for (y in 0..15) {
                for (x in 0..15) {
                    val distFromCenter = Math.hypot((x - 7.5), (y - 7.5))
                    val col = when {
                        distFromCenter < 3.5 -> 0xFFFFFFAA.toInt() // White-hot core
                        distFromCenter < 5.5 -> 0xFFFFEE00.toInt() // Bright sulfur yellow
                        distFromCenter < 7.5 -> 0xFFFF9900.toInt() // Energetic orange rim
                        x == 0 || y == 0 || x == 15 || y == 15 -> 0xFFCC7700.toInt() // Border
                        else -> 0xFFFFBB00.toInt()
                    }
                    px(c, sx + x, sy + y, col)
                }
            }
        }

        // 78: Sulfur Bricks (Sulfur stone masonry with dark mortar)
        drawTile(78) { c, sx, sy ->
            val rnd = Random(78004L)
            for (y in 0..15) {
                for (x in 0..15) {
                    val isMortar = (y % 4 == 0) || (y < 4 && x == 8) || (y in 4..7 && (x == 0 || x == 15)) || (y in 8..11 && x == 8) || (y in 12..15 && (x == 0 || x == 15))
                    if (isMortar) {
                        px(c, sx + x, sy + y, 0xFF3E3618.toInt())
                    } else {
                        val base = 190 + rnd.nextInt(40)
                        px(c, sx + x, sy + y, Color.rgb(base, (base * 0.85f).toInt(), 25))
                    }
                }
            }
        }

        // 79: Sulfur Geothermal Vent (Dark basalt with bubbling sulfur vent)
        drawTile(79) { c, sx, sy ->
            val rnd = Random(79005L)
            for (y in 0..15) {
                for (x in 0..15) {
                    val dist = Math.hypot((x - 7.5), (y - 7.5))
                    if (dist < 4.5) {
                        // Bubbling sulfur pool
                        val col = if (dist < 2.0) 0xFFFFF555.toInt() else 0xFFFFB300.toInt()
                        px(c, sx + x, sy + y, col)
                    } else {
                        // Dark volcanic basalt
                        val v = 45 + rnd.nextInt(30)
                        px(c, sx + x, sy + y, Color.rgb(v + 10, v + 8, v))
                    }
                }
            }
        }

        // 80: Sulfur Crystal Cluster / Dust
        drawTile(80) { c, sx, sy ->
            for (y in 0..15) for (x in 0..15) px(c, sx + x, sy + y, Color.TRANSPARENT)
            // Draw sharp crystal cluster
            val crystalPixels = listOf(
                Pair(7, 2), Pair(8, 2),
                Pair(6, 3), Pair(7, 3), Pair(8, 3), Pair(9, 3),
                Pair(6, 4), Pair(7, 4), Pair(8, 4), Pair(9, 4),
                Pair(5, 5), Pair(6, 5), Pair(7, 5), Pair(8, 5), Pair(9, 5), Pair(10, 5),
                Pair(4, 6), Pair(5, 6), Pair(6, 6), Pair(7, 6), Pair(8, 6), Pair(9, 6), Pair(10, 6), Pair(11, 6),
                Pair(4, 7), Pair(5, 7), Pair(7, 7), Pair(8, 7), Pair(10, 7), Pair(11, 7),
                Pair(3, 8), Pair(4, 8), Pair(7, 8), Pair(8, 8), Pair(11, 8), Pair(12, 8),
                Pair(3, 9), Pair(4, 9), Pair(6, 9), Pair(7, 9), Pair(8, 9), Pair(9, 9),
                Pair(4, 10), Pair(5, 10), Pair(6, 10), Pair(7, 10), Pair(8, 10), Pair(9, 10),
                Pair(5, 11), Pair(6, 11), Pair(7, 11), Pair(8, 11), Pair(9, 11),
                Pair(6, 12), Pair(7, 12), Pair(8, 12),
                Pair(7, 13)
            )
            for ((cx, cy) in crystalPixels) {
                val col = if (cx == 7 || cy == 3 || cy == 4) 0xFFFFFF88.toInt() else 0xFFFFD700.toInt()
                px(c, sx + cx, sy + cy, col)
            }
        }

        // 81: Chicken / Feather item
        drawTile(81) { c, sx, sy ->
            for (y in 0..15) for (x in 0..15) px(c, sx + x, sy + y, Color.TRANSPARENT)
            for (i in 0..11) {
                px(c, sx + 2 + i, sy + 13 - i, 0xFFE0E0E0.toInt())
                px(c, sx + 3 + i, sy + 13 - i, 0xFFFFFFFF.toInt())
                px(c, sx + 2 + i, sy + 12 - i, 0xFFB0B0B0.toInt())
            }
            px(c, sx + 13, sy + 2, 0xFF888888.toInt())
            px(c, sx + 2, sy + 13, 0xFFDDDDDD.toInt())
        }

        // 82: Dragon Egg / Dragon's Breath
        drawTile(82) { c, sx, sy ->
            for (y in 0..15) for (x in 0..15) px(c, sx + x, sy + y, Color.TRANSPARENT)
            // Egg shape
            for (y in 3..13) {
                val w = when (y) {
                    3 -> 2
                    4, 5 -> 3
                    6, 7, 8, 9 -> 4
                    10, 11 -> 3
                    12 -> 2
                    else -> 1
                }
                for (x in (8 - w)..(8 + w)) {
                    val col = when {
                        x == 8 - w || y == 3 || y == 13 -> 0xFF180A22.toInt()
                        (x + y) % 3 == 0 -> 0xFF8E24AA.toInt() // Purple spots
                        else -> 0xFF241030.toInt() // Obsidian dark
                    }
                    px(c, sx + x, sy + y, col)
                }
            }
        }

        // Breaking damage cracks 0..9 (tiles 48..57)
        for (stage in 0..9) {
            drawTile(48 + stage) { c, sx, sy ->
                for (y in 0..15) for (x in 0..15) px(c, sx + x, sy + y, Color.TRANSPARENT)
                val cCrack = Color.argb(120 + stage * 13, 10, 10, 10)
                val rndCrack = Random(stage.toLong() * 37L + 7L)
                val crackLines = 3 + stage * 2
                paint.color = cCrack
                for (i in 0 until crackLines) {
                    val x1 = sx + rndCrack.nextInt(16)
                    val y1 = sy + rndCrack.nextInt(16)
                    val x2 = sx + rndCrack.nextInt(16)
                    val y2 = sy + rndCrack.nextInt(16)
                    c.drawLine(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat(), paint)
                }
            }
        }

        return bitmap
    }
}
