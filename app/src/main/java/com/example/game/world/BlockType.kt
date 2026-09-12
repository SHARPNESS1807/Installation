package com.example.game.world

/**
 * All block types available in Minecraft Java 26.2.
 */
enum class BlockType(
    val id: Byte,
    val displayName: String,
    val isSolid: Boolean = true,
    val isTransparent: Boolean = false,
    val isLiquid: Boolean = false,
    val hardness: Float = 1.0f,
    val lightEmission: Int = 0,
    val topTex: Int = 0,
    val bottomTex: Int = 0,
    val sideTex: Int = 0
) {
    AIR(0, "Air", isSolid = false, isTransparent = true, hardness = 0f),
    GRASS(1, "Grass Block", hardness = 0.6f, topTex = 0, bottomTex = 2, sideTex = 1),
    DIRT(2, "Dirt", hardness = 0.5f, topTex = 2, bottomTex = 2, sideTex = 2),
    STONE(3, "Stone", hardness = 1.5f, topTex = 3, bottomTex = 3, sideTex = 3),
    COBBLESTONE(4, "Cobblestone", hardness = 2.0f, topTex = 4, bottomTex = 4, sideTex = 4),
    BEDROCK(5, "Bedrock", hardness = -1.0f, topTex = 5, bottomTex = 5, sideTex = 5),
    OAK_LOG(6, "Oak Log", hardness = 2.0f, topTex = 6, bottomTex = 6, sideTex = 7),
    OAK_PLANKS(7, "Oak Planks", hardness = 1.8f, topTex = 8, bottomTex = 8, sideTex = 8),
    OAK_LEAVES(8, "Oak Leaves", isSolid = true, isTransparent = true, hardness = 0.2f, topTex = 9, bottomTex = 9, sideTex = 9),
    SAND(9, "Sand", hardness = 0.5f, topTex = 10, bottomTex = 10, sideTex = 10),
    WATER(10, "Water", isSolid = false, isTransparent = true, isLiquid = true, hardness = 100f, topTex = 11, bottomTex = 11, sideTex = 11),
    GLASS(11, "Glass", isSolid = true, isTransparent = true, hardness = 0.3f, topTex = 12, bottomTex = 12, sideTex = 12),
    COAL_ORE(12, "Coal Ore", hardness = 3.0f, topTex = 13, bottomTex = 13, sideTex = 13),
    IRON_ORE(13, "Iron Ore", hardness = 3.0f, topTex = 14, bottomTex = 14, sideTex = 14),
    GOLD_ORE(14, "Gold Ore", hardness = 3.0f, topTex = 15, bottomTex = 15, sideTex = 15),
    DIAMOND_ORE(15, "Diamond Ore", hardness = 3.0f, topTex = 16, bottomTex = 16, sideTex = 16),
    REDSTONE_ORE(16, "Redstone Ore", hardness = 3.0f, lightEmission = 5, topTex = 17, bottomTex = 17, sideTex = 17),
    CRAFTING_TABLE(17, "Crafting Table", hardness = 2.5f, topTex = 18, bottomTex = 8, sideTex = 19),
    FURNACE(18, "Furnace", hardness = 3.5f, topTex = 20, bottomTex = 20, sideTex = 21),
    TNT(19, "TNT", hardness = 0.0f, topTex = 22, bottomTex = 23, sideTex = 24),
    BRICKS(20, "Bricks", hardness = 2.0f, topTex = 25, bottomTex = 25, sideTex = 25),
    BOOKSHELF(21, "Bookshelf", hardness = 1.5f, topTex = 8, bottomTex = 8, sideTex = 26),
    TORCH(22, "Torch", isSolid = false, isTransparent = true, hardness = 0f, lightEmission = 14, topTex = 27, bottomTex = 27, sideTex = 27),
    FLOWER_RED(23, "Poppy", isSolid = false, isTransparent = true, hardness = 0f, topTex = 28, bottomTex = 28, sideTex = 28),
    FLOWER_YELLOW(24, "Dandelion", isSolid = false, isTransparent = true, hardness = 0f, topTex = 29, bottomTex = 29, sideTex = 29),
    SANDSTONE(25, "Sandstone", hardness = 0.8f, topTex = 46, bottomTex = 46, sideTex = 47),
    SNOW(26, "Snow Block", hardness = 0.2f, topTex = 60, bottomTex = 60, sideTex = 60),
    BIRCH_LOG(27, "Birch Log", hardness = 2.0f, topTex = 6, bottomTex = 6, sideTex = 61),
    BIRCH_LEAVES(28, "Birch Leaves", isSolid = true, isTransparent = true, hardness = 0.2f, topTex = 62, bottomTex = 62, sideTex = 62),
    COPPER_BLOCK(29, "Block of Copper", hardness = 3.0f, topTex = 64, bottomTex = 64, sideTex = 64),
    CRAFTER(30, "Crafter", hardness = 2.5f, topTex = 65, bottomTex = 8, sideTex = 66),
    CHERRY_PLANKS(31, "Cherry Planks", hardness = 1.8f, topTex = 67, bottomTex = 67, sideTex = 67),
    TUFF(32, "Tuff", hardness = 1.5f, topTex = 68, bottomTex = 68, sideTex = 68),
    SULFUR_ORE(33, "Sulfur Ore", hardness = 3.0f, topTex = 75, bottomTex = 75, sideTex = 75),
    SULFUR_BLOCK(34, "Block of Sulfur", hardness = 2.5f, lightEmission = 4, topTex = 76, bottomTex = 76, sideTex = 76),
    SULFUR_CUBE(35, "Sulfur Cube", hardness = 1.0f, lightEmission = 12, topTex = 77, bottomTex = 77, sideTex = 77),
    SULFUR_BRICKS(36, "Sulfur Bricks", hardness = 2.8f, topTex = 78, bottomTex = 78, sideTex = 78),
    SULFUR_VENT(37, "Sulfur Geothermal Vent", hardness = 3.5f, lightEmission = 10, topTex = 79, bottomTex = 3, sideTex = 79),
    SULFUR_CRYSTAL(38, "Sulfur Crystal Cluster", isSolid = false, isTransparent = true, hardness = 0.5f, lightEmission = 8, topTex = 80, bottomTex = 80, sideTex = 80);

    companion object {
        private val byId = entries.associateBy { it.id }

        fun fromId(id: Byte): BlockType = byId[id] ?: AIR
    }
}
