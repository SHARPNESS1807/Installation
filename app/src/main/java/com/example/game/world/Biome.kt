package com.example.game.world

/**
 * Java Edition biomes with unique terrain characteristics and foliage.
 */
enum class Biome(
    val id: String,
    val displayName: String,
    val topBlock: BlockType,
    val underBlock: BlockType,
    val baseHeight: Int,
    val heightVariation: Int,
    val treeChance: Float,
    val flowerChance: Float,
    val hasBirch: Boolean = false
) {
    PLAINS(
        id = "minecraft:plains",
        displayName = "Plains",
        topBlock = BlockType.GRASS,
        underBlock = BlockType.DIRT,
        baseHeight = 18,
        heightVariation = 5,
        treeChance = 0.015f,
        flowerChance = 0.05f
    ),
    FOREST(
        id = "minecraft:forest",
        displayName = "Forest",
        topBlock = BlockType.GRASS,
        underBlock = BlockType.DIRT,
        baseHeight = 19,
        heightVariation = 6,
        treeChance = 0.065f,
        flowerChance = 0.04f,
        hasBirch = true
    ),
    DESERT(
        id = "minecraft:desert",
        displayName = "Desert",
        topBlock = BlockType.SAND,
        underBlock = BlockType.SANDSTONE,
        baseHeight = 17,
        heightVariation = 4,
        treeChance = 0.0f,
        flowerChance = 0.0f
    ),
    WINDSWEPT_HILLS(
        id = "minecraft:windswept_hills",
        displayName = "Windswept Hills",
        topBlock = BlockType.STONE,
        underBlock = BlockType.STONE,
        baseHeight = 25,
        heightVariation = 14,
        treeChance = 0.01f,
        flowerChance = 0.01f
    ),
    SNOWY_PLAINS(
        id = "minecraft:snowy_plains",
        displayName = "Snowy Plains",
        topBlock = BlockType.SNOW,
        underBlock = BlockType.DIRT,
        baseHeight = 19,
        heightVariation = 4,
        treeChance = 0.01f,
        flowerChance = 0.0f
    ),
    OCEAN(
        id = "minecraft:ocean",
        displayName = "Ocean",
        topBlock = BlockType.SAND,
        underBlock = BlockType.DIRT,
        baseHeight = 11,
        heightVariation = 3,
        treeChance = 0.0f,
        flowerChance = 0.0f
    ),
    SULFUR_CAVES(
        id = "minecraft:sulfur_caves",
        displayName = "Sulfur Caves",
        topBlock = BlockType.GRASS,
        underBlock = BlockType.DIRT,
        baseHeight = 18,
        heightVariation = 5,
        treeChance = 0.02f,
        flowerChance = 0.03f
    )
}
