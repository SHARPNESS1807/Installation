package com.example.game.crafting

import com.example.game.world.ItemStack
import com.example.game.world.ItemType

data class Recipe(
    val name: String,
    val result: ItemStack,
    val ingredients: List<Pair<ItemType, Int>>,
    val description: String = ""
)

object CraftingManager {
    val recipes = listOf(
        Recipe(
            name = "Oak Planks",
            result = ItemStack(ItemType.ITEM_OAK_PLANKS, 4),
            ingredients = listOf(ItemType.ITEM_OAK_LOG to 1),
            description = "Basic building material and crafting component."
        ),
        Recipe(
            name = "Sticks",
            result = ItemStack(ItemType.STICK, 4),
            ingredients = listOf(ItemType.ITEM_OAK_PLANKS to 2),
            description = "Used for handles of tools and torches."
        ),
        Recipe(
            name = "Crafting Table",
            result = ItemStack(ItemType.ITEM_CRAFTING_TABLE, 1),
            ingredients = listOf(ItemType.ITEM_OAK_PLANKS to 4),
            description = "Essential station for complex crafting."
        ),
        Recipe(
            name = "Torches",
            result = ItemStack(ItemType.ITEM_TORCH, 4),
            ingredients = listOf(ItemType.STICK to 1, ItemType.COAL to 1),
            description = "Provides warm light and prevents mob spawns."
        ),
        Recipe(
            name = "Furnace",
            result = ItemStack(ItemType.ITEM_FURNACE, 1),
            ingredients = listOf(ItemType.ITEM_COBBLESTONE to 8),
            description = "Smelts ores into ingots."
        ),
        Recipe(
            name = "Wooden Pickaxe",
            result = ItemStack(ItemType.WOODEN_PICKAXE, 1),
            ingredients = listOf(ItemType.ITEM_OAK_PLANKS to 3, ItemType.STICK to 2),
            description = "Mines stone and coal."
        ),
        Recipe(
            name = "Wooden Sword",
            result = ItemStack(ItemType.WOODEN_SWORD, 1),
            ingredients = listOf(ItemType.ITEM_OAK_PLANKS to 2, ItemType.STICK to 1),
            description = "Basic defense weapon."
        ),
        Recipe(
            name = "Stone Pickaxe",
            result = ItemStack(ItemType.STONE_PICKAXE, 1),
            ingredients = listOf(ItemType.ITEM_COBBLESTONE to 3, ItemType.STICK to 2),
            description = "Mines iron and coal faster."
        ),
        Recipe(
            name = "Stone Sword",
            result = ItemStack(ItemType.STONE_SWORD, 1),
            ingredients = listOf(ItemType.ITEM_COBBLESTONE to 2, ItemType.STICK to 1),
            description = "Deals +5 attack damage."
        ),
        Recipe(
            name = "Iron Pickaxe",
            result = ItemStack(ItemType.IRON_PICKAXE, 1),
            ingredients = listOf(ItemType.IRON_INGOT to 3, ItemType.STICK to 2),
            description = "Mines gold, redstone, and diamond ores."
        ),
        Recipe(
            name = "Iron Sword",
            result = ItemStack(ItemType.IRON_SWORD, 1),
            ingredients = listOf(ItemType.IRON_INGOT to 2, ItemType.STICK to 1),
            description = "Deals +6 attack damage."
        ),
        Recipe(
            name = "Diamond Pickaxe",
            result = ItemStack(ItemType.DIAMOND_PICKAXE, 1),
            ingredients = listOf(ItemType.DIAMOND to 3, ItemType.STICK to 2),
            description = "Fastest and most durable pickaxe."
        ),
        Recipe(
            name = "Diamond Sword",
            result = ItemStack(ItemType.DIAMOND_SWORD, 1),
            ingredients = listOf(ItemType.DIAMOND to 2, ItemType.STICK to 1),
            description = "Deals +7 attack damage."
        ),
        Recipe(
            name = "TNT",
            result = ItemStack(ItemType.ITEM_TNT, 1),
            ingredients = listOf(ItemType.COAL to 4, ItemType.ITEM_SAND to 4),
            description = "High explosive block."
        ),
        Recipe(
            name = "Bricks",
            result = ItemStack(ItemType.ITEM_BRICKS, 4),
            ingredients = listOf(ItemType.ITEM_DIRT to 4),
            description = "Refined decorative masonry."
        ),
        Recipe(
            name = "Bookshelf",
            result = ItemStack(ItemType.ITEM_BOOKSHELF, 1),
            ingredients = listOf(ItemType.ITEM_OAK_PLANKS to 6, ItemType.STICK to 3),
            description = "Cozy library block."
        ),
        Recipe(
            name = "Bread",
            result = ItemStack(ItemType.BREAD, 1),
            ingredients = listOf(ItemType.ITEM_OAK_LEAVES to 3),
            description = "Nourishing baked food."
        ),
        Recipe(
            name = "Shield",
            result = ItemStack(ItemType.SHIELD, 1),
            ingredients = listOf(ItemType.ITEM_OAK_PLANKS to 6, ItemType.IRON_INGOT to 1),
            description = "Java Edition defensive shield for off-hand dual wielding."
        ),
        Recipe(
            name = "Sandstone",
            result = ItemStack(ItemType.ITEM_SANDSTONE, 4),
            ingredients = listOf(ItemType.ITEM_SAND to 4),
            description = "Solid desert masonry block."
        ),
        Recipe(
            name = "Block of Copper",
            result = ItemStack(ItemType.ITEM_COPPER_BLOCK, 1),
            ingredients = listOf(ItemType.COPPER_INGOT to 4),
            description = "Official metallic copper block from Tricky Trials."
        ),
        Recipe(
            name = "Crafter (1.21)",
            result = ItemStack(ItemType.ITEM_CRAFTER, 1),
            ingredients = listOf(ItemType.IRON_INGOT to 4, ItemType.ITEM_CRAFTING_TABLE to 1),
            description = "Signature 1.21 automated redstone crafter."
        ),
        Recipe(
            name = "Cherry Planks (1.20)",
            result = ItemStack(ItemType.ITEM_CHERRY_PLANKS, 4),
            ingredients = listOf(ItemType.ITEM_OAK_PLANKS to 2, ItemType.ITEM_FLOWER_RED to 1),
            description = "Warm pastel pink wood planks."
        ),
        Recipe(
            name = "Tuff",
            result = ItemStack(ItemType.ITEM_TUFF, 4),
            ingredients = listOf(ItemType.ITEM_COBBLESTONE to 2, ItemType.ITEM_DIRT to 2),
            description = "Volcanic stone block found in Trial Chambers."
        ),
        Recipe(
            name = "Wind Charge (1.21)",
            result = ItemStack(ItemType.WIND_CHARGE, 4),
            ingredients = listOf(ItemType.ITEM_SNOW to 2, ItemType.STICK to 1),
            description = "Breeze projectile providing launch propulsion."
        ),
        Recipe(
            name = "Mace (1.21)",
            result = ItemStack(ItemType.MACE, 1),
            ingredients = listOf(ItemType.DIAMOND to 2, ItemType.STICK to 2, ItemType.ITEM_COPPER_BLOCK to 1),
            description = "Heavy Trial Chamber weapon with destructive smash attacks."
        ),
        Recipe(
            name = "Netherite Sword",
            result = ItemStack(ItemType.NETHERITE_SWORD, 1),
            ingredients = listOf(ItemType.DIAMOND_SWORD to 1, ItemType.NETHERITE_INGOT to 1),
            description = "Upgraded +8 damage sword forged with Netherite."
        ),
        Recipe(
            name = "Netherite Pickaxe",
            result = ItemStack(ItemType.NETHERITE_PICKAXE, 1),
            ingredients = listOf(ItemType.DIAMOND_PICKAXE to 1, ItemType.NETHERITE_INGOT to 1),
            description = "Peak efficiency pickaxe capable of mining any block instantly."
        )
    )

    fun canCraft(recipe: Recipe, inventory: Array<ItemStack?>): Boolean {
        for ((requiredItem, requiredCount) in recipe.ingredients) {
            val totalInInventory = inventory.filterNotNull()
                .filter { it.item == requiredItem }
                .sumOf { it.count }
            if (totalInInventory < requiredCount) return false
        }
        return true
    }

    fun craft(recipe: Recipe, inventory: Array<ItemStack?>): Boolean {
        if (!canCraft(recipe, inventory)) return false

        // Deduct ingredients
        for ((requiredItem, requiredCount) in recipe.ingredients) {
            var remainingToDeduct = requiredCount
            for (slot in inventory.indices) {
                val stack = inventory[slot] ?: continue
                if (stack.item == requiredItem) {
                    val deduct = minOf(stack.count, remainingToDeduct)
                    stack.count -= deduct
                    remainingToDeduct -= deduct
                    if (stack.count <= 0) {
                        inventory[slot] = null
                    }
                    if (remainingToDeduct <= 0) break
                }
            }
        }

        // Add result
        var added = false
        for (slot in inventory.indices) {
            val stack = inventory[slot]
            if (stack != null && stack.item == recipe.result.item && stack.count + recipe.result.count <= recipe.result.item.maxStackSize) {
                stack.count += recipe.result.count
                added = true
                break
            }
        }
        if (!added) {
            for (slot in inventory.indices) {
                if (inventory[slot] == null) {
                    inventory[slot] = recipe.result.copyStack()
                    added = true
                    break
                }
            }
        }

        if (added) {
            when (recipe.result.item) {
                ItemType.ITEM_CRAFTING_TABLE -> com.example.game.advancements.AdvancementManager.trigger(com.example.game.advancements.AdvancementManager.BENCHMARKING)
                ItemType.WOODEN_PICKAXE, ItemType.STONE_PICKAXE, ItemType.IRON_PICKAXE, ItemType.DIAMOND_PICKAXE ->
                    com.example.game.advancements.AdvancementManager.trigger(com.example.game.advancements.AdvancementManager.TIME_TO_MINE)
                ItemType.WOODEN_SWORD, ItemType.STONE_SWORD, ItemType.IRON_SWORD, ItemType.DIAMOND_SWORD ->
                    com.example.game.advancements.AdvancementManager.trigger(com.example.game.advancements.AdvancementManager.TIME_TO_STRIKE)
                ItemType.ITEM_FURNACE -> com.example.game.advancements.AdvancementManager.trigger(com.example.game.advancements.AdvancementManager.HOT_TOPIC)
                ItemType.SHIELD -> com.example.game.advancements.AdvancementManager.trigger(com.example.game.advancements.AdvancementManager.NOT_TODAY)
                else -> {}
            }
        }

        return added
    }
}
