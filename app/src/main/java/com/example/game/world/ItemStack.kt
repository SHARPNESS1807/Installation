package com.example.game.world

/**
 * An item stack representing an item type and count (1..64).
 */
data class ItemStack(
    val item: ItemType,
    var count: Int = 1
) {
    fun copyStack(newCount: Int = count): ItemStack = ItemStack(item, newCount)

    val isFull: Boolean get() = count >= item.maxStackSize
}
