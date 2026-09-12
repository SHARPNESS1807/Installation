package com.example.game.world

import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * High-performance Infinite 3D Voxel World representation for Minecraft Java 26.2.
 * Powered by procedural multi-octave gradient noise, dynamic chunk loading,
 * multi-biome distribution, ore distribution, caves, and Amanatides & Woo DDA raycasting.
 */
class World(
    val seed: Long = 133742L,
    val name: String = "New World",
    val isFlat: Boolean = false
) {
    companion object {
        const val CHUNK_SIZE = Chunk.CHUNK_SIZE // 16
        const val CHUNK_HEIGHT = Chunk.CHUNK_HEIGHT // 48
        const val WATER_LEVEL = 15
        const val DEFAULT_RADIUS = 3 // 7x7 = 49 chunks active
    }

    // Infinite chunk repository keyed by packed (cx, cz)
    val loadedChunks = ConcurrentHashMap<Long, Chunk>()

    private val noiseGen = WorldNoise(seed)

    // Day / Night cycle (0 to 24000 ticks)
    var timeOfDay: Long = 6000L // Noon
    var daySpeedMultiplier: Float = 1.0f

    // Weather
    var isRaining: Boolean = false
    var rainTimer: Float = 0f

    init {
        // Pre-generate initial chunks around origin (0, 0)
        updateChunksAround(0f, 0f, DEFAULT_RADIUS)
    }

    fun getChunk(cx: Int, cz: Int): Chunk? {
        return loadedChunks[Chunk.packKey(cx, cz)]
    }

    fun getOrCreateChunk(cx: Int, cz: Int): Chunk {
        val key = Chunk.packKey(cx, cz)
        return loadedChunks.computeIfAbsent(key) {
            val chunk = Chunk(cx, cz)
            generateChunk(chunk)
            chunk
        }
    }

    fun isValid(x: Int, y: Int, z: Int): Boolean = y in 0 until CHUNK_HEIGHT

    fun getBlock(x: Int, y: Int, z: Int): BlockType {
        if (y < 0 || y >= CHUNK_HEIGHT) return BlockType.AIR
        val cx = floor(x.toDouble() / CHUNK_SIZE).toInt()
        val cz = floor(z.toDouble() / CHUNK_SIZE).toInt()
        val chunk = getOrCreateChunk(cx, cz)
        val lx = ((x % CHUNK_SIZE) + CHUNK_SIZE) % CHUNK_SIZE
        val lz = ((z % CHUNK_SIZE) + CHUNK_SIZE) % CHUNK_SIZE
        return chunk.getBlock(lx, y, lz)
    }

    fun setBlock(x: Int, y: Int, z: Int, type: BlockType) {
        if (y < 0 || y >= CHUNK_HEIGHT) return
        val cx = floor(x.toDouble() / CHUNK_SIZE).toInt()
        val cz = floor(z.toDouble() / CHUNK_SIZE).toInt()
        val chunk = getOrCreateChunk(cx, cz)
        val lx = ((x % CHUNK_SIZE) + CHUNK_SIZE) % CHUNK_SIZE
        val lz = ((z % CHUNK_SIZE) + CHUNK_SIZE) % CHUNK_SIZE
        chunk.setBlock(lx, y, lz, type)

        // Mark neighboring chunk dirty if on the boundary
        if (lx == 0) getChunk(cx - 1, cz)?.isDirty = true
        if (lx == CHUNK_SIZE - 1) getChunk(cx + 1, cz)?.isDirty = true
        if (lz == 0) getChunk(cx, cz - 1)?.isDirty = true
        if (lz == CHUNK_SIZE - 1) getChunk(cx, cz + 1)?.isDirty = true
    }

    fun getBiomeAt(worldX: Int, worldZ: Int): Biome {
        if (isFlat) return Biome.PLAINS

        val continental = noiseGen.perlin2D(worldX * 0.005, worldZ * 0.005)
        val temp = noiseGen.perlin2D(worldX * 0.009 + 250.0, worldZ * 0.009 + 250.0)
        val sulfurNoise = noiseGen.perlin2D(worldX * 0.015 + 800.0, worldZ * 0.015 + 800.0)
        val elevation = noiseGen.fbm2D(worldX * 0.03, worldZ * 0.03, 2)

        return when {
            sulfurNoise > 0.45 -> Biome.SULFUR_CAVES
            continental < -0.38 -> Biome.OCEAN
            temp > 0.42 -> Biome.DESERT
            temp < -0.42 -> Biome.SNOWY_PLAINS
            elevation > 0.48 -> Biome.WINDSWEPT_HILLS
            temp in 0.05..0.4 -> Biome.FOREST
            else -> Biome.PLAINS
        }
    }

    private fun generateChunk(chunk: Chunk) {
        if (chunk.isGenerated) return
        chunk.isGenerated = true

        val cx = chunk.cx
        val cz = chunk.cz

        if (isFlat) {
            for (lx in 0 until CHUNK_SIZE) {
                for (lz in 0 until CHUNK_SIZE) {
                    chunk.setBlockFast(lx, 0, lz, BlockType.BEDROCK)
                    for (y in 1..2) chunk.setBlockFast(lx, y, lz, BlockType.DIRT)
                    chunk.setBlockFast(lx, 3, lz, BlockType.GRASS)
                }
            }
            return
        }

        // Heightmap per column
        val heightMap = IntArray(CHUNK_SIZE * CHUNK_SIZE)
        val biomeMap = Array(CHUNK_SIZE * CHUNK_SIZE) { Biome.PLAINS }

        for (lx in 0 until CHUNK_SIZE) {
            for (lz in 0 until CHUNK_SIZE) {
                val wx = cx * CHUNK_SIZE + lx
                val wz = cz * CHUNK_SIZE + lz

                val biome = getBiomeAt(wx, wz)
                biomeMap[lx * CHUNK_SIZE + lz] = biome

                // Continuous elevation across infinite chunks
                val base = noiseGen.perlin2D(wx * 0.012, wz * 0.012)
                val detail = noiseGen.fbm2D(wx * 0.04, wz * 0.04, 3)

                val h = when (biome) {
                    Biome.OCEAN -> (biome.baseHeight + base * 3).toInt().coerceIn(8, WATER_LEVEL - 1)
                    Biome.WINDSWEPT_HILLS -> (biome.baseHeight + base * 8 + detail * 10).toInt().coerceIn(20, CHUNK_HEIGHT - 6)
                    Biome.DESERT -> (biome.baseHeight + base * 4 + detail * 2).toInt().coerceIn(15, 24)
                    else -> (biome.baseHeight + base * 4 + detail * 3).toInt().coerceIn(14, 26)
                }

                heightMap[lx * CHUNK_SIZE + lz] = h

                // Bedrock at bottom
                chunk.setBlockFast(lx, 0, lz, BlockType.BEDROCK)

                // Fill underground and surface
                for (y in 1..h) {
                    val block = when {
                        y < h - 3 -> BlockType.STONE
                        y < h -> biome.underBlock
                        else -> {
                            // Surface block
                            if (y <= WATER_LEVEL && biome != Biome.DESERT) {
                                BlockType.SAND
                            } else if (biome == Biome.WINDSWEPT_HILLS && y > 33) {
                                BlockType.SNOW
                            } else {
                                biome.topBlock
                            }
                        }
                    }
                    chunk.setBlockFast(lx, y, lz, block)
                }

                // Fill water bodies up to WATER_LEVEL
                if (h < WATER_LEVEL) {
                    for (y in (h + 1)..WATER_LEVEL) {
                        chunk.setBlockFast(lx, y, lz, BlockType.WATER)
                    }
                }
            }
        }

        // Chunk deterministic RNG for ores, caves, trees
        val chunkSeed = seed xor (cx.toLong() * 341873128712L + cz.toLong() * 132897987541L)
        val rng = Random(chunkSeed)

        // 1. Ores
        generateChunkOres(chunk, rng)

        // 2. Caves (3D carved air worms)
        generateChunkCaves(chunk, rng)

        // 3. Foliage & Trees
        for (lx in 2 until CHUNK_SIZE - 2) {
            for (lz in 2 until CHUNK_SIZE - 2) {
                val h = heightMap[lx * CHUNK_SIZE + lz]
                val biome = biomeMap[lx * CHUNK_SIZE + lz]
                val surfaceBlock = chunk.getBlock(lx, h, lz)

                if (h > WATER_LEVEL && (surfaceBlock == BlockType.GRASS || surfaceBlock == BlockType.SNOW)) {
                    val roll = rng.nextFloat()
                    if (roll < biome.treeChance) {
                        growChunkTree(chunk, lx, h + 1, lz, biome.hasBirch && rng.nextBoolean(), rng)
                    } else if (roll < biome.treeChance + biome.flowerChance) {
                        val flower = if (rng.nextBoolean()) BlockType.FLOWER_RED else BlockType.FLOWER_YELLOW
                        chunk.setBlockFast(lx, h + 1, lz, flower)
                    }
                }
            }
        }
    }

    private fun generateChunkOres(chunk: Chunk, rng: Random) {
        // Coal (y: 2..36)
        generateVeinsInChunk(chunk, BlockType.COAL_ORE, count = 10, radius = 2, minY = 2, maxY = 36, rng)
        // Iron (y: 2..28)
        generateVeinsInChunk(chunk, BlockType.IRON_ORE, count = 6, radius = 2, minY = 2, maxY = 28, rng)
        // Gold (y: 2..16)
        generateVeinsInChunk(chunk, BlockType.GOLD_ORE, count = 3, radius = 1, minY = 2, maxY = 16, rng)
        // Redstone (y: 1..12)
        generateVeinsInChunk(chunk, BlockType.REDSTONE_ORE, count = 2, radius = 1, minY = 1, maxY = 12, rng)
        // Diamond (y: 1..8)
        generateVeinsInChunk(chunk, BlockType.DIAMOND_ORE, count = 2, radius = 1, minY = 1, maxY = 8, rng)
        // Sulfur Ore (y: 3..20 strictly underground)
        generateVeinsInChunk(chunk, BlockType.SULFUR_ORE, count = 6, radius = 2, minY = 3, maxY = 20, rng)
        // Sulfur Blocks & Cubes strictly underground in Sulfur biomes (y: 3..18)
        if (chunk.biome == Biome.SULFUR_CAVES) {
            generateVeinsInChunk(chunk, BlockType.SULFUR_BLOCK, count = 8, radius = 2, minY = 3, maxY = 18, rng)
            generateVeinsInChunk(chunk, BlockType.SULFUR_CUBE, count = 4, radius = 1, minY = 3, maxY = 16, rng)
            generateVeinsInChunk(chunk, BlockType.SULFUR_BRICKS, count = 4, radius = 2, minY = 3, maxY = 18, rng)
        }
    }

    private fun generateVeinsInChunk(
        chunk: Chunk,
        ore: BlockType,
        count: Int,
        radius: Int,
        minY: Int,
        maxY: Int,
        rng: Random
    ) {
        for (i in 0 until count) {
            val vx = rng.nextInt(1, CHUNK_SIZE - 1)
            val vy = rng.nextInt(minY, maxY)
            val vz = rng.nextInt(1, CHUNK_SIZE - 1)

            for (dx in -radius..radius) {
                for (dy in -radius..radius) {
                    for (dz in -radius..radius) {
                        if (dx * dx + dy * dy + dz * dz <= radius * radius) {
                            val ox = vx + dx
                            val oy = vy + dy
                            val oz = vz + dz
                            if (ox in 0 until CHUNK_SIZE && oz in 0 until CHUNK_SIZE && oy in 1 until CHUNK_HEIGHT) {
                                if (chunk.getBlock(ox, oy, oz) == BlockType.STONE) {
                                    chunk.setBlockFast(ox, oy, oz, ore)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun generateChunkCaves(chunk: Chunk, rng: Random) {
        val caveCount = rng.nextInt(1, 3)
        for (i in 0 until caveCount) {
            var cx = rng.nextInt(2, CHUNK_SIZE - 2).toFloat()
            var cy = rng.nextInt(6, 20).toFloat()
            var cz = rng.nextInt(2, CHUNK_SIZE - 2).toFloat()

            val steps = rng.nextInt(6, 12)
            for (s in 0 until steps) {
                val r = 2
                for (dx in -r..r) {
                    for (dy in -r..r) {
                        for (dz in -r..r) {
                            if (dx * dx + dy * dy + dz * dz <= r * r) {
                                val bx = (cx + dx).toInt()
                                val by = (cy + dy).toInt()
                                val bz = (cz + dz).toInt()
                                if (bx in 0 until CHUNK_SIZE && bz in 0 until CHUNK_SIZE && by in 2 until CHUNK_HEIGHT) {
                                    if (chunk.getBlock(bx, by, bz) == BlockType.STONE) {
                                        chunk.setBlockFast(bx, by, bz, BlockType.AIR)
                                        // Sulfur cave lining & vents strictly underground (y in 3..18)
                                        if (chunk.biome == Biome.SULFUR_CAVES && by in 3..18) {
                                            if (chunk.getBlock(bx, by - 1, bz) == BlockType.STONE && rng.nextFloat() < 0.20f) {
                                                val floorBlock = when (rng.nextInt(5)) {
                                                    0 -> BlockType.SULFUR_VENT
                                                    1 -> BlockType.SULFUR_CUBE
                                                    2 -> BlockType.SULFUR_CRYSTAL
                                                    else -> BlockType.SULFUR_BLOCK
                                                }
                                                chunk.setBlockFast(bx, by, bz, floorBlock)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                cx += (rng.nextFloat() * 2f - 1f) * 1.2f
                cy += (rng.nextFloat() * 2f - 1f) * 0.4f
                cz += (rng.nextFloat() * 2f - 1f) * 1.2f
            }
        }
    }

    private fun growChunkTree(chunk: Chunk, x: Int, y: Int, z: Int, isBirch: Boolean, rng: Random) {
        val trunkHeight = 4 + rng.nextInt(2)
        if (y + trunkHeight + 2 >= CHUNK_HEIGHT) return

        val logType = if (isBirch) BlockType.BIRCH_LOG else BlockType.OAK_LOG
        val leafType = if (isBirch) BlockType.BIRCH_LEAVES else BlockType.OAK_LEAVES

        // Trunk
        for (ty in 0 until trunkHeight) {
            chunk.setBlockFast(x, y + ty, z, logType)
        }

        // Leaves canopy
        val leafBottom = y + trunkHeight - 2
        val leafTop = y + trunkHeight + 1
        for (ly in leafBottom..leafTop) {
            val r = if (ly == leafTop) 1 else 2
            for (dx in -r..r) {
                for (dz in -r..r) {
                    if (dx == 0 && dz == 0 && ly < y + trunkHeight) continue
                    if (abs(dx) == r && abs(dz) == r && rng.nextFloat() > 0.6f) continue
                    val lx = x + dx
                    val lz = z + dz
                    if (lx in 0 until CHUNK_SIZE && lz in 0 until CHUNK_SIZE) {
                        if (chunk.getBlock(lx, ly, lz) == BlockType.AIR) {
                            chunk.setBlockFast(lx, ly, lz, leafType)
                        }
                    }
                }
            }
        }
    }

    /**
     * Dynamically loads/generates chunks in radius around player and unloads distant ones.
     */
    fun updateChunksAround(playerX: Float, playerZ: Float, radius: Int = DEFAULT_RADIUS) {
        val playerChunkX = floor(playerX.toDouble() / CHUNK_SIZE).toInt()
        val playerChunkZ = floor(playerZ.toDouble() / CHUNK_SIZE).toInt()

        // Load chunks in radius
        for (cx in (playerChunkX - radius)..(playerChunkX + radius)) {
            for (cz in (playerChunkZ - radius)..(playerChunkZ + radius)) {
                getOrCreateChunk(cx, cz)
            }
        }

        // Unload chunks far away (> radius + 2) to protect mobile RAM
        val maxDistSq = (radius + 2) * (radius + 2)
        val iterator = loadedChunks.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val cx = Chunk.unpackX(entry.key)
            val cz = Chunk.unpackZ(entry.key)
            val distSq = (cx - playerChunkX) * (cx - playerChunkX) + (cz - playerChunkZ) * (cz - playerChunkZ)
            if (distSq > maxDistSq) {
                iterator.remove()
            }
        }
    }

    fun getSpawnPosition(): Triple<Float, Float, Float> {
        val spawnX = 0f
        val spawnZ = 0f
        var highestY = WATER_LEVEL + 2
        for (y in CHUNK_HEIGHT - 4 downTo 1) {
            val block = getBlock(0, y, 0)
            if (block.isSolid) {
                highestY = y + 2
                break
            }
        }
        return Triple(spawnX + 0.5f, highestY.toFloat(), spawnZ + 0.5f)
    }

    fun tick(deltaSeconds: Float) {
        timeOfDay = (timeOfDay + (20f * deltaSeconds * daySpeedMultiplier).toLong()) % 24000L

        // Rain weather cycle
        rainTimer += deltaSeconds
        if (rainTimer > 600f) {
            rainTimer = 0f
            isRaining = !isRaining
        }
    }

    val sunAngle: Float
        get() = (timeOfDay.toFloat() / 24000f) * 360f

    val isDaytime: Boolean
        get() = timeOfDay in 0..12000

    val ambientLight: Float
        get() {
            val angleRad = Math.toRadians(sunAngle.toDouble())
            val sinVal = sin(angleRad).toFloat()
            val base = (0.22f + 0.78f * max(0f, sinVal)).coerceIn(0.2f, 1.0f)
            return if (isRaining) base * 0.75f else base
        }

    /**
     * Amanatides & Woo DDA Fast Voxel Traversal across infinite world.
     */
    fun raycast(
        originX: Float, originY: Float, originZ: Float,
        dirX: Float, dirY: Float, dirZ: Float,
        maxDistance: Float = 5.2f
    ): RaycastResult {
        val len = sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ)
        if (len == 0f) return RaycastResult(hit = false)
        val dx = dirX / len
        val dy = dirY / len
        val dz = dirZ / len

        var currentX = floor(originX).toInt()
        var currentY = floor(originY).toInt()
        var currentZ = floor(originZ).toInt()

        val stepX = if (dx > 0) 1 else if (dx < 0) -1 else 0
        val stepY = if (dy > 0) 1 else if (dy < 0) -1 else 0
        val stepZ = if (dz > 0) 1 else if (dz < 0) -1 else 0

        val tDeltaX = if (dx != 0f) abs(1f / dx) else Float.MAX_VALUE
        val tDeltaY = if (dy != 0f) abs(1f / dy) else Float.MAX_VALUE
        val tDeltaZ = if (dz != 0f) abs(1f / dz) else Float.MAX_VALUE

        var tMaxX = if (dx > 0) (currentX + 1 - originX) * tDeltaX else (originX - currentX) * tDeltaX
        var tMaxY = if (dy > 0) (currentY + 1 - originY) * tDeltaY else (originY - currentY) * tDeltaY
        var tMaxZ = if (dz > 0) (currentZ + 1 - originZ) * tDeltaZ else (originZ - currentZ) * tDeltaZ

        var normalX = 0
        var normalY = 0
        var normalZ = 0

        var distance = 0f

        while (distance <= maxDistance) {
            if (currentY in 0 until CHUNK_HEIGHT) {
                val block = getBlock(currentX, currentY, currentZ)
                if (block != BlockType.AIR && block != BlockType.WATER) {
                    return RaycastResult(
                        hit = true,
                        blockX = currentX,
                        blockY = currentY,
                        blockZ = currentZ,
                        normalX = normalX,
                        normalY = normalY,
                        normalZ = normalZ,
                        blockType = block,
                        distance = distance
                    )
                }
            }

            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    currentX += stepX
                    distance = tMaxX
                    tMaxX += tDeltaX
                    normalX = -stepX
                    normalY = 0
                    normalZ = 0
                } else {
                    currentZ += stepZ
                    distance = tMaxZ
                    tMaxZ += tDeltaZ
                    normalX = 0
                    normalY = 0
                    normalZ = -stepZ
                }
            } else {
                if (tMaxY < tMaxZ) {
                    currentY += stepY
                    distance = tMaxY
                    tMaxY += tDeltaY
                    normalX = 0
                    normalY = -stepY
                    normalZ = 0
                } else {
                    currentZ += stepZ
                    distance = tMaxZ
                    tMaxZ += tDeltaZ
                    normalX = 0
                    normalY = 0
                    normalZ = -stepZ
                }
            }
        }

        return RaycastResult(hit = false)
    }
}
