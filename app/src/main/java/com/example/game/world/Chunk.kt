package com.example.game.world

/**
 * Represents a single 16x48x16 voxel chunk in the infinite world.
 */
class Chunk(
    val cx: Int,
    val cz: Int
) {
    companion object {
        const val CHUNK_SIZE = 16
        const val CHUNK_HEIGHT = 48

        fun packKey(cx: Int, cz: Int): Long {
            return (cx.toLong() and 0xFFFFFFFFL) shl 32 or (cz.toLong() and 0xFFFFFFFFL)
        }

        fun unpackX(key: Long): Int {
            return (key shr 32).toInt()
        }

        fun unpackZ(key: Long): Int {
            return key.toInt()
        }
    }

    val blocks = ByteArray(CHUNK_SIZE * CHUNK_HEIGHT * CHUNK_SIZE)
    var isDirty = true
    var isGenerated = false
    var biome: Biome = Biome.PLAINS

    @Suppress("NOTHING_TO_INLINE")
    private inline fun getIndex(lx: Int, y: Int, lz: Int): Int {
        return (lx * CHUNK_HEIGHT + y) * CHUNK_SIZE + lz
    }

    fun getBlock(lx: Int, y: Int, lz: Int): BlockType {
        if (lx !in 0 until CHUNK_SIZE || y !in 0 until CHUNK_HEIGHT || lz !in 0 until CHUNK_SIZE) {
            return BlockType.AIR
        }
        val id = blocks[getIndex(lx, y, lz)]
        return BlockType.fromId(id)
    }

    fun setBlock(lx: Int, y: Int, lz: Int, type: BlockType) {
        if (lx !in 0 until CHUNK_SIZE || y !in 0 until CHUNK_HEIGHT || lz !in 0 until CHUNK_SIZE) {
            return
        }
        blocks[getIndex(lx, y, lz)] = type.id
        isDirty = true
    }

    fun setBlockFast(lx: Int, y: Int, lz: Int, type: BlockType) {
        blocks[getIndex(lx, y, lz)] = type.id
    }
}
