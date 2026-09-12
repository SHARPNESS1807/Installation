package com.example.game.entities

import com.example.game.world.BlockType
import com.example.game.world.ItemStack
import com.example.game.world.ItemType
import com.example.game.world.World
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

enum class GameMode(val displayName: String) {
    SURVIVAL("Survival"),
    HARDCORE("Hardcore"),
    CREATIVE("Creative"),
    SPECTATOR("Spectator")
}

/**
 * First-person player entity with AABB physics, inventory, survival stats,
 * camera rotation, and block interaction.
 */
class Player(
    var x: Float = 32.5f,
    var y: Float = 22.0f,
    var z: Float = 32.5f
) {
    var yaw: Float = 0f    // Horizontal camera angle in degrees
    var pitch: Float = 0f  // Vertical camera angle (-89..89)

    var vx: Float = 0f
    var vy: Float = 0f
    var vz: Float = 0f

    var gameMode: GameMode = GameMode.SURVIVAL

    enum class CameraPerspective {
        FIRST_PERSON,
        THIRD_PERSON_BACK,
        THIRD_PERSON_FRONT
    }
    var cameraPerspective: CameraPerspective = CameraPerspective.FIRST_PERSON
    var showHitboxes: Boolean = false

    // Player dimensions (AABB)
    val width = 0.6f
    val height = 1.8f
    val eyeHeight = 1.62f

    var isGrounded = false
    var isInWater = false
    var isSneaking = false
    var isSprinting = false
    var isFlying = false

    // Survival stats
    var health: Int = 2000
    var maxHealth: Int = 2000
    var hunger: Int = 20
    var maxHunger: Int = 20
    var armor: Int = 0
    var expLevel: Int = 0
    var expProgress: Float = 0.35f
    var hurtTimer: Float = 0f

    // Inventory: 9 hotbar slots + 27 inventory slots = 36 total
    val inventory = Array<ItemStack?>(36) { null }
    var selectedHotbarSlot: Int = 0

    // Java Edition Off-hand Slot (Dual Wielding)
    var offhandItem: ItemStack? = ItemStack(ItemType.SHIELD, 1)

    // Java Edition Combat: Attack cooldown (0.0 to 1.0)
    var attackCooldown: Float = 1.0f

    // Interaction & Animation
    var handSwingTimer: Float = 0f
    var miningProgress: Float = 0f
    var miningBlockX: Int = -1
    var miningBlockY: Int = -1
    var miningBlockZ: Int = -1

    init {
        // Starter items in survival
        inventory[0] = ItemStack(ItemType.WOODEN_PICKAXE, 1)
        inventory[1] = ItemStack(ItemType.WOODEN_SWORD, 1)
        inventory[2] = ItemStack(ItemType.ITEM_OAK_PLANKS, 32)
        inventory[3] = ItemStack(ItemType.ITEM_TORCH, 16)
        inventory[4] = ItemStack(ItemType.APPLE, 8)
        inventory[5] = ItemStack(ItemType.ITEM_COBBLESTONE, 64)
    }

    val selectedItemStack: ItemStack?
        get() = inventory[selectedHotbarSlot]

    fun swapHands() {
        val temp = inventory[selectedHotbarSlot]
        inventory[selectedHotbarSlot] = offhandItem
        offhandItem = temp
        if (offhandItem?.item == ItemType.SHIELD) {
            com.example.game.advancements.AdvancementManager.trigger(com.example.game.advancements.AdvancementManager.NOT_TODAY)
        }
    }

    fun dropSelectedItem(): ItemStack? {
        val stack = inventory[selectedHotbarSlot] ?: return null
        return if (stack.count > 1) {
            stack.count -= 1
            ItemStack(stack.item, 1)
        } else {
            inventory[selectedHotbarSlot] = null
            stack
        }
    }

    fun cycleGameMode() {
        gameMode = when (gameMode) {
            GameMode.SURVIVAL -> GameMode.CREATIVE
            GameMode.CREATIVE -> GameMode.SPECTATOR
            GameMode.SPECTATOR -> GameMode.HARDCORE
            GameMode.HARDCORE -> GameMode.SURVIVAL
        }
    }

    fun cycleCameraPerspective() {
        cameraPerspective = when (cameraPerspective) {
            CameraPerspective.FIRST_PERSON -> CameraPerspective.THIRD_PERSON_BACK
            CameraPerspective.THIRD_PERSON_BACK -> CameraPerspective.THIRD_PERSON_FRONT
            CameraPerspective.THIRD_PERSON_FRONT -> CameraPerspective.FIRST_PERSON
        }
    }

    fun addExperience(amount: Int) {
        val total = expProgress + (amount.toFloat() / 15f)
        expLevel += total.toInt()
        expProgress = total % 1f
    }

    fun triggerHandSwing() {
        handSwingTimer = 0.25f
        attackCooldown = 0.0f
    }

    fun takeDamage(amount: Int) {
        if (gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR || hurtTimer > 0) return
        var finalDamage = amount
        if (offhandItem?.item == ItemType.SHIELD) {
            finalDamage = (finalDamage * 0.35f).toInt().coerceAtLeast(1)
            com.example.game.advancements.AdvancementManager.trigger(com.example.game.advancements.AdvancementManager.NOT_TODAY)
        }
        health = (health - finalDamage).coerceAtLeast(0)
        hurtTimer = 0.4f
    }

    fun heal(amount: Int) {
        health = (health + amount).coerceAtMost(maxHealth)
    }

    fun feed(amount: Int) {
        hunger = (hunger + amount).coerceAtMost(maxHunger)
    }

    fun updatePhysics(world: World, forwardInput: Float, strafeInput: Float, jumpInput: Boolean, dt: Float) {
        if (hurtTimer > 0) hurtTimer -= dt
        if (handSwingTimer > 0) handSwingTimer -= dt
        if (attackCooldown < 1.0f) attackCooldown = (attackCooldown + dt * 2.5f).coerceAtMost(1.0f)

        // Track infinite distance for Adventuring Time advancement
        val distFromSpawn = kotlin.math.sqrt(x * x + z * z)
        if (distFromSpawn > 200f) {
            com.example.game.advancements.AdvancementManager.trigger(com.example.game.advancements.AdvancementManager.ADVENTURING_TIME)
        }

        // Camera direction vectors in XZ plane
        val yawRad = Math.toRadians(yaw.toDouble())
        val forwardX = -sin(yawRad).toFloat()
        val forwardZ = cos(yawRad).toFloat()
        val rightX = cos(yawRad).toFloat()
        val rightZ = sin(yawRad).toFloat()

        // Movement speed
        val baseSpeed = if (isSprinting) 6.8f else if (isSneaking) 2.2f else 4.3f
        val moveSpeed = if (isFlying) baseSpeed * 1.8f else baseSpeed

        val wishDirX = forwardX * forwardInput + rightX * strafeInput
        val wishDirZ = forwardZ * forwardInput + rightZ * strafeInput

        // Spectator Mode: Full 3D noclip flight through blocks
        if (gameMode == GameMode.SPECTATOR) {
            val specSpeed = 10.5f * (if (isSprinting) 2.2f else 1.0f)
            val pitchRad = Math.toRadians(pitch.toDouble())
            val lookDirY = -sin(pitchRad).toFloat()
            vx = wishDirX * specSpeed
            vz = wishDirZ * specSpeed
            vy = (lookDirY * forwardInput * specSpeed) + (if (jumpInput) specSpeed * 0.8f else if (isSneaking) -specSpeed * 0.8f else 0f)

            x += vx * dt
            y += vy * dt
            z += vz * dt
            isGrounded = false
            return
        }

        // Check water
        val blockAtFeet = world.getBlock(floor(x).toInt(), floor(y).toInt(), floor(z).toInt())
        val blockAtEyes = world.getBlock(floor(x).toInt(), floor(y + eyeHeight).toInt(), floor(z).toInt())
        isInWater = blockAtFeet == BlockType.WATER || blockAtEyes == BlockType.WATER

        if (isFlying && gameMode == GameMode.CREATIVE) {
            vx = wishDirX * moveSpeed
            vz = wishDirZ * moveSpeed
            vy = if (jumpInput) moveSpeed * 0.8f else if (isSneaking) -moveSpeed * 0.8f else 0f
        } else if (isInWater) {
            vx = wishDirX * moveSpeed * 0.55f
            vz = wishDirZ * moveSpeed * 0.55f
            vy = if (jumpInput) 3.2f else -1.2f
        } else {
            // Horizontal deceleration & acceleration
            val friction = if (isGrounded) 12f else 3.5f
            val targetVx = wishDirX * moveSpeed
            val targetVz = wishDirZ * moveSpeed

            vx += (targetVx - vx) * (friction * dt).coerceAtMost(1f)
            vz += (targetVz - vz) * (friction * dt).coerceAtMost(1f)

            // Jump
            if (jumpInput && isGrounded) {
                vy = 8.2f
                isGrounded = false
            }

            // Gravity
            vy -= 22f * dt
            if (vy < -35f) vy = -35f
        }

        // Apply movement with AABB collision resolution
        moveWithCollision(world, vx * dt, vy * dt, vz * dt)

        // Hunger / Health regeneration over time
        if (gameMode == GameMode.SURVIVAL || gameMode == GameMode.HARDCORE) {
            if (hunger >= 18 && health < maxHealth) {
                heal(1)
            }
        }
    }

    private fun moveWithCollision(world: World, dx: Float, dy: Float, dz: Float) {
        val halfW = width / 2f

        // Y-axis movement first
        var newY = y + dy
        if (dy < 0) {
            // Falling down
            val minX = floor(x - halfW).toInt()
            val maxX = floor(x + halfW).toInt()
            val minZ = floor(z - halfW).toInt()
            val maxZ = floor(z + halfW).toInt()
            val checkY = floor(newY).toInt()

            var collided = false
            for (bx in minX..maxX) {
                for (bz in minZ..maxZ) {
                    if (world.getBlock(bx, checkY, bz).isSolid) {
                        collided = true
                        break
                    }
                }
                if (collided) break
            }

            if (collided) {
                newY = checkY + 1.0f
                vy = 0f
                isGrounded = true
            } else {
                isGrounded = false
            }
        } else if (dy > 0) {
            // Jumping up
            val minX = floor(x - halfW).toInt()
            val maxX = floor(x + halfW).toInt()
            val minZ = floor(z - halfW).toInt()
            val maxZ = floor(z + halfW).toInt()
            val checkY = floor(newY + height).toInt()

            var collided = false
            for (bx in minX..maxX) {
                for (bz in minZ..maxZ) {
                    if (world.getBlock(bx, checkY, bz).isSolid) {
                        collided = true
                        break
                    }
                }
                if (collided) break
            }

            if (collided) {
                newY = checkY - height - 0.01f
                vy = 0f
            }
            isGrounded = false
        }
        y = newY.coerceIn(0.5f, (World.CHUNK_HEIGHT - 2).toFloat())

        // X-axis collision
        var newX = x + dx
        val checkX = if (dx > 0) floor(newX + halfW).toInt() else floor(newX - halfW).toInt()
        val minZ = floor(z - halfW).toInt()
        val maxZ = floor(z + halfW).toInt()
        val minY = floor(y).toInt()
        val maxY = floor(y + height - 0.1f).toInt()

        var xCollided = false
        for (by in minY..maxY) {
            for (bz in minZ..maxZ) {
                if (world.getBlock(checkX, by, bz).isSolid) {
                    xCollided = true
                    break
                }
            }
            if (xCollided) break
        }

        if (xCollided) {
            // Auto step-up for 1 block
            val stepBlock = world.getBlock(checkX, minY, (minZ + maxZ) / 2)
            val aboveStepBlock = world.getBlock(checkX, minY + 1, (minZ + maxZ) / 2)
            val headSpace = world.getBlock(floor(x).toInt(), minY + 2, (minZ + maxZ) / 2)
            if (isGrounded && stepBlock.isSolid && !aboveStepBlock.isSolid && !headSpace.isSolid) {
                y += 1.05f
                x = newX
            } else {
                vx = 0f
            }
        } else {
            x = newX
        }

        // Z-axis collision
        var newZ = z + dz
        val checkZ = if (dz > 0) floor(newZ + halfW).toInt() else floor(newZ - halfW).toInt()
        val minX2 = floor(x - halfW).toInt()
        val maxX2 = floor(x + halfW).toInt()

        var zCollided = false
        for (by in minY..maxY) {
            for (bx in minX2..maxX2) {
                if (world.getBlock(bx, by, checkZ).isSolid) {
                    zCollided = true
                    break
                }
            }
            if (zCollided) break
        }

        if (zCollided) {
            // Auto step-up for 1 block
            val stepBlock = world.getBlock((minX2 + maxX2) / 2, minY, checkZ)
            val aboveStepBlock = world.getBlock((minX2 + maxX2) / 2, minY + 1, checkZ)
            val headSpace = world.getBlock((minX2 + maxX2) / 2, minY + 2, floor(z).toInt())
            if (isGrounded && stepBlock.isSolid && !aboveStepBlock.isSolid && !headSpace.isSolid) {
                y += 1.05f
                z = newZ
            } else {
                vz = 0f
            }
        } else {
            z = newZ
        }
    }

    fun addItem(item: ItemType, count: Int = 1): Boolean {
        // Try stacking
        for (slot in inventory.indices) {
            val existing = inventory[slot]
            if (existing != null && existing.item == item && existing.count < item.maxStackSize) {
                val canAdd = min(count, item.maxStackSize - existing.count)
                existing.count += canAdd
                val remaining = count - canAdd
                if (remaining == 0) return true
                return addItem(item, remaining)
            }
        }
        // Try empty slot
        for (slot in inventory.indices) {
            if (inventory[slot] == null) {
                inventory[slot] = ItemStack(item, count)
                return true
            }
        }
        return false
    }

    fun consumeSelectedItem(): Boolean {
        val current = selectedItemStack ?: return false
        current.count--
        if (current.count <= 0) {
            inventory[selectedHotbarSlot] = null
        }
        return true
    }
}
