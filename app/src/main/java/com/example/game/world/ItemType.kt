package com.example.game.world

/**
 * Items available in Java 26.2 (Tools, Weapons, Materials, Food, Blocks).
 */
enum class ItemType(
    val id: String,
    val displayName: String,
    val blockType: BlockType? = null,
    val maxStackSize: Int = 64,
    val attackDamage: Float = 1f,
    val miningSpeedMultiplier: Float = 1f,
    val foodRestoration: Int = 0,
    val iconTextureIdx: Int = 0
) {
    // Blocks as items
    ITEM_GRASS("grass", "Grass Block", BlockType.GRASS, iconTextureIdx = 0),
    ITEM_DIRT("dirt", "Dirt", BlockType.DIRT, iconTextureIdx = 2),
    ITEM_STONE("stone", "Stone", BlockType.STONE, iconTextureIdx = 3),
    ITEM_COBBLESTONE("cobblestone", "Cobblestone", BlockType.COBBLESTONE, iconTextureIdx = 4),
    ITEM_BEDROCK("bedrock", "Bedrock", BlockType.BEDROCK, iconTextureIdx = 5),
    ITEM_OAK_LOG("oak_log", "Oak Log", BlockType.OAK_LOG, iconTextureIdx = 7),
    ITEM_OAK_PLANKS("oak_planks", "Oak Planks", BlockType.OAK_PLANKS, iconTextureIdx = 8),
    ITEM_OAK_LEAVES("oak_leaves", "Oak Leaves", BlockType.OAK_LEAVES, iconTextureIdx = 9),
    ITEM_SAND("sand", "Sand", BlockType.SAND, iconTextureIdx = 10),
    ITEM_GLASS("glass", "Glass", BlockType.GLASS, iconTextureIdx = 12),
    ITEM_COAL_ORE("coal_ore", "Coal Ore", BlockType.COAL_ORE, iconTextureIdx = 13),
    ITEM_IRON_ORE("iron_ore", "Iron Ore", BlockType.IRON_ORE, iconTextureIdx = 14),
    ITEM_GOLD_ORE("gold_ore", "Gold Ore", BlockType.GOLD_ORE, iconTextureIdx = 15),
    ITEM_DIAMOND_ORE("diamond_ore", "Diamond Ore", BlockType.DIAMOND_ORE, iconTextureIdx = 16),
    ITEM_REDSTONE_ORE("redstone_ore", "Redstone Ore", BlockType.REDSTONE_ORE, iconTextureIdx = 17),
    ITEM_CRAFTING_TABLE("crafting_table", "Crafting Table", BlockType.CRAFTING_TABLE, iconTextureIdx = 18),
    ITEM_FURNACE("furnace", "Furnace", BlockType.FURNACE, iconTextureIdx = 21),
    ITEM_TNT("tnt", "TNT", BlockType.TNT, iconTextureIdx = 24),
    ITEM_BRICKS("bricks", "Bricks", BlockType.BRICKS, iconTextureIdx = 25),
    ITEM_BOOKSHELF("bookshelf", "Bookshelf", BlockType.BOOKSHELF, iconTextureIdx = 26),
    ITEM_TORCH("torch", "Torch", BlockType.TORCH, iconTextureIdx = 27),
    ITEM_FLOWER_RED("flower_red", "Poppy", BlockType.FLOWER_RED, iconTextureIdx = 28),
    ITEM_FLOWER_YELLOW("flower_yellow", "Dandelion", BlockType.FLOWER_YELLOW, iconTextureIdx = 29),
    ITEM_SANDSTONE("sandstone", "Sandstone", BlockType.SANDSTONE, iconTextureIdx = 47),
    ITEM_SNOW("snow", "Snow Block", BlockType.SNOW, iconTextureIdx = 60),
    ITEM_BIRCH_LOG("birch_log", "Birch Log", BlockType.BIRCH_LOG, iconTextureIdx = 61),
    ITEM_BIRCH_LEAVES("birch_leaves", "Birch Leaves", BlockType.BIRCH_LEAVES, iconTextureIdx = 62),
    ITEM_COPPER_BLOCK("copper_block", "Block of Copper", BlockType.COPPER_BLOCK, iconTextureIdx = 64),
    ITEM_CRAFTER("crafter", "Crafter", BlockType.CRAFTER, iconTextureIdx = 65),
    ITEM_CHERRY_PLANKS("cherry_planks", "Cherry Planks", BlockType.CHERRY_PLANKS, iconTextureIdx = 67),
    ITEM_TUFF("tuff", "Tuff", BlockType.TUFF, iconTextureIdx = 68),
    ITEM_SULFUR_ORE("sulfur_ore", "Sulfur Ore", BlockType.SULFUR_ORE, iconTextureIdx = 75),
    ITEM_SULFUR_BLOCK("sulfur_block", "Block of Sulfur", BlockType.SULFUR_BLOCK, iconTextureIdx = 76),
    ITEM_SULFUR_CUBE("sulfur_cube", "Sulfur Cube", BlockType.SULFUR_CUBE, iconTextureIdx = 77),
    ITEM_SULFUR_BRICKS("sulfur_bricks", "Sulfur Bricks", BlockType.SULFUR_BRICKS, iconTextureIdx = 78),
    ITEM_SULFUR_VENT("sulfur_vent", "Sulfur Geothermal Vent", BlockType.SULFUR_VENT, iconTextureIdx = 79),
    ITEM_SULFUR_CRYSTAL("sulfur_crystal", "Sulfur Crystal Cluster", BlockType.SULFUR_CRYSTAL, iconTextureIdx = 80),

    // Tools & Combat
    SHIELD("shield", "Shield", maxStackSize = 1, attackDamage = 1f, iconTextureIdx = 63),
    MACE("mace", "Mace", maxStackSize = 1, attackDamage = 8f, miningSpeedMultiplier = 2.0f, iconTextureIdx = 71),
    WOODEN_PICKAXE("wooden_pickaxe", "Wooden Pickaxe", maxStackSize = 1, attackDamage = 2f, miningSpeedMultiplier = 2.0f, iconTextureIdx = 30),
    STONE_PICKAXE("stone_pickaxe", "Stone Pickaxe", maxStackSize = 1, attackDamage = 3f, miningSpeedMultiplier = 4.0f, iconTextureIdx = 31),
    IRON_PICKAXE("iron_pickaxe", "Iron Pickaxe", maxStackSize = 1, attackDamage = 4f, miningSpeedMultiplier = 6.0f, iconTextureIdx = 32),
    DIAMOND_PICKAXE("diamond_pickaxe", "Diamond Pickaxe", maxStackSize = 1, attackDamage = 5f, miningSpeedMultiplier = 8.0f, iconTextureIdx = 33),
    NETHERITE_PICKAXE("netherite_pickaxe", "Netherite Pickaxe", maxStackSize = 1, attackDamage = 6f, miningSpeedMultiplier = 10.0f, iconTextureIdx = 74),

    WOODEN_SWORD("wooden_sword", "Wooden Sword", maxStackSize = 1, attackDamage = 4f, miningSpeedMultiplier = 1.5f, iconTextureIdx = 34),
    STONE_SWORD("stone_sword", "Stone Sword", maxStackSize = 1, attackDamage = 5f, miningSpeedMultiplier = 1.5f, iconTextureIdx = 35),
    IRON_SWORD("iron_sword", "Iron Sword", maxStackSize = 1, attackDamage = 6f, miningSpeedMultiplier = 1.5f, iconTextureIdx = 36),
    DIAMOND_SWORD("diamond_sword", "Diamond Sword", maxStackSize = 1, attackDamage = 7f, miningSpeedMultiplier = 1.5f, iconTextureIdx = 37),
    NETHERITE_SWORD("netherite_sword", "Netherite Sword", maxStackSize = 1, attackDamage = 8f, miningSpeedMultiplier = 1.5f, iconTextureIdx = 73),

    // Materials
    STICK("stick", "Stick", iconTextureIdx = 38),
    COAL("coal", "Coal", iconTextureIdx = 39),
    IRON_INGOT("iron_ingot", "Iron Ingot", iconTextureIdx = 40),
    GOLD_INGOT("gold_ingot", "Gold Ingot", iconTextureIdx = 41),
    DIAMOND("diamond", "Diamond", iconTextureIdx = 42),
    COPPER_INGOT("copper_ingot", "Copper Ingot", iconTextureIdx = 69),
    WIND_CHARGE("wind_charge", "Wind Charge", iconTextureIdx = 70),
    NETHERITE_INGOT("netherite_ingot", "Netherite Ingot", iconTextureIdx = 72),
    SULFUR_DUST("sulfur_dust", "Sulfur Dust", iconTextureIdx = 80),
    FEATHER("feather", "Feather", iconTextureIdx = 81),
    WHITE_WOOL("white_wool", "White Wool", iconTextureIdx = 8),
    DRAGON_EGG("dragon_egg", "Dragon Egg", maxStackSize = 1, iconTextureIdx = 82),
    DRAGON_BREATH("dragon_breath", "Dragon's Breath", maxStackSize = 16, iconTextureIdx = 82),

    // Food
    APPLE("apple", "Apple", foodRestoration = 4, iconTextureIdx = 43),
    BREAD("bread", "Bread", foodRestoration = 5, iconTextureIdx = 44),
    COOKED_PORKCHOP("cooked_porkchop", "Cooked Porkchop", foodRestoration = 8, iconTextureIdx = 45),
    RAW_CHICKEN("raw_chicken", "Raw Chicken", foodRestoration = 2, iconTextureIdx = 81),
    COOKED_CHICKEN("cooked_chicken", "Cooked Chicken", foodRestoration = 6, iconTextureIdx = 81),
    RAW_BEEF("raw_beef", "Raw Beef", foodRestoration = 3, iconTextureIdx = 45),
    STEAK("steak", "Steak", foodRestoration = 8, iconTextureIdx = 45);

    val isTool: Boolean get() = maxStackSize == 1
    val isFood: Boolean get() = foodRestoration > 0

    companion object {
        fun fromBlock(block: BlockType): ItemType {
            return entries.firstOrNull { it.blockType == block } ?: ITEM_DIRT
        }

        fun fromId(id: String): ItemType {
            return entries.firstOrNull { it.id == id } ?: ITEM_DIRT
        }
    }
}
