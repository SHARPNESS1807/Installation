package com.example.game.world

data class RaycastResult(
    val hit: Boolean,
    val blockX: Int = 0,
    val blockY: Int = 0,
    val blockZ: Int = 0,
    val normalX: Int = 0,
    val normalY: Int = 0,
    val normalZ: Int = 0,
    val blockType: BlockType = BlockType.AIR,
    val distance: Float = 0f
) {
    val placeX: Int get() = blockX + normalX
    val placeY: Int get() = blockY + normalY
    val placeZ: Int get() = blockZ + normalZ
}
