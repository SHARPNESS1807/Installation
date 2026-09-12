package com.example.game.entities

import com.example.game.world.ItemType
import com.example.game.world.World
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

enum class MobType(
    val displayName: String,
    val isHostile: Boolean,
    val maxHealth: Int,
    val speed: Float,
    val dropItem: ItemType,
    val bodyColorR: Float,
    val bodyColorG: Float,
    val bodyColorB: Float,
    val scale: Float = 1.0f,
    val isFlyingBoss: Boolean = false
) {
    COW("Cow", false, 10, 1.5f, ItemType.STEAK, 0.45f, 0.28f, 0.18f, scale = 1.1f),
    SHEEP("Sheep", false, 8, 1.5f, ItemType.WHITE_WOOL, 0.92f, 0.92f, 0.92f, scale = 1.0f),
    CHICKEN("Chicken", false, 4, 1.4f, ItemType.COOKED_CHICKEN, 0.98f, 0.98f, 0.98f, scale = 0.55f),
    PIG("Pig", false, 10, 1.8f, ItemType.COOKED_PORKCHOP, 0.95f, 0.6f, 0.65f, scale = 0.9f),
    ZOMBIE("Zombie", true, 20, 2.2f, ItemType.ITEM_DIRT, 0.15f, 0.55f, 0.25f, scale = 1.0f),
    SKELETON("Skeleton", true, 16, 2.4f, ItemType.STICK, 0.8f, 0.8f, 0.8f, scale = 1.0f),
    CREEPER("Creeper", true, 18, 2.0f, ItemType.COAL, 0.2f, 0.75f, 0.2f, scale = 1.0f),
    SPIDER("Spider", true, 16, 2.6f, ItemType.STICK, 0.15f, 0.15f, 0.15f, scale = 0.8f),
    ENDERMAN("Enderman", true, 40, 2.8f, ItemType.DIAMOND, 0.08f, 0.08f, 0.08f, scale = 1.35f),
    IRON_GOLEM("Iron Golem", false, 100, 1.6f, ItemType.IRON_INGOT, 0.85f, 0.82f, 0.78f, scale = 1.4f),
    VILLAGER("Villager", false, 20, 1.5f, ItemType.APPLE, 0.6f, 0.4f, 0.25f, scale = 1.0f),
    WITHER("Wither", true, 300, 3.8f, ItemType.NETHERITE_INGOT, 0.15f, 0.15f, 0.15f, scale = 2.4f, isFlyingBoss = true),
    SULFUR_CUBE("Sulfur Cube", true, 16, 2.0f, ItemType.ITEM_SULFUR_CUBE, 1.0f, 0.92f, 0.08f, scale = 0.9f),
    ENDER_DRAGON("Ender Dragon", true, 200, 4.2f, ItemType.DRAGON_EGG, 0.12f, 0.04f, 0.18f, scale = 3.8f, isFlyingBoss = true)
}

class Mob(
    val type: MobType,
    var x: Float,
    var y: Float,
    var z: Float
) {
    var health: Int = type.maxHealth
    var yaw: Float = 0f
    var pitch: Float = 0f
    var walkAnimTimer: Float = 0f
    var hurtTimer: Float = 0f
    var wingFlapTimer: Float = 0f

    private var wanderTimer: Float = 0f
    private var wanderDirX: Float = 0f
    private var wanderDirZ: Float = 0f
    private var dragonOrbitAngle: Float = Random.nextFloat() * 6.28f
    private var dragonAltitude: Float = 28f
    private var dragonSwoopTimer: Float = 8f

    val isAlive: Boolean get() = health > 0

    fun update(world: World, player: Player, dt: Float) {
        if (!isAlive) return
        if (hurtTimer > 0) hurtTimer -= dt
        walkAnimTimer += dt * 5f
        wingFlapTimer += dt * (if (type == MobType.ENDER_DRAGON) 8f else 12f)

        // Special Boss Flight AI for Ender Dragon
        if (type.isFlyingBoss) {
            updateEnderDragon(player, dt)
            return
        }

        val dx = player.x - x
        val dy = player.y - y
        val dz = player.z - z
        val distToPlayer = sqrt(dx * dx + dz * dz)

        var moveX = 0f
        var moveZ = 0f

        if (type.isHostile && distToPlayer < 14f && player.gameMode != GameMode.CREATIVE && player.gameMode != GameMode.SPECTATOR) {
            // Chase player
            val angle = atan2(dx.toDouble(), dz.toDouble()).toFloat()
            yaw = Math.toDegrees(angle.toDouble()).toFloat()
            moveX = (dx / distToPlayer) * type.speed
            moveZ = (dz / distToPlayer) * type.speed

            // Attack player if very close
            if (distToPlayer < 1.3f && kotlin.math.abs(dy) < 1.8f) {
                player.takeDamage(if (type == MobType.CREEPER) 10 else if (type == MobType.SULFUR_CUBE) 5 else 3)
            }
        } else {
            // Wander peacefully
            wanderTimer -= dt
            if (wanderTimer <= 0) {
                wanderTimer = Random.nextFloat() * 3f + 1.5f
                val angle = Random.nextFloat() * Math.PI.toFloat() * 2f
                wanderDirX = sin(angle) * type.speed * 0.5f
                wanderDirZ = cos(angle) * type.speed * 0.5f
                yaw = Math.toDegrees(angle.toDouble()).toFloat()
            }
            moveX = wanderDirX
            moveZ = wanderDirZ
        }

        // Apply movement step
        val newX = x + moveX * dt
        val newZ = z + moveZ * dt

        val blockBelow = world.getBlock(newX.toInt(), (y - 0.2f).toInt(), newZ.toInt())
        val blockAtMob = world.getBlock(newX.toInt(), y.toInt(), newZ.toInt())

        if (blockAtMob.isSolid) {
            // Jump step up
            y += 1.0f
        } else if (!blockBelow.isSolid) {
            // Fall or flutter
            val fallSpeed = if (type == MobType.CHICKEN) 2.5f else 8f
            y = (y - fallSpeed * dt).coerceAtLeast(1f)
        }

        // Sulfur cube slime bouncy hop
        if (type == MobType.SULFUR_CUBE && (walkAnimTimer % 2.5f) < 0.3f && blockBelow.isSolid) {
            y += 0.8f
        }

        x = newX
        z = newZ
    }

    private fun updateEnderDragon(player: Player, dt: Float) {
        dragonOrbitAngle += dt * 0.8f
        dragonSwoopTimer -= dt

        if (dragonSwoopTimer <= 0f) {
            // Swoop down at player
            val targetX = player.x
            val targetY = player.y + 1f
            val targetZ = player.z

            val dx = targetX - x
            val dy = targetY - y
            val dz = targetZ - z
            val dist = sqrt(dx * dx + dy * dy + dz * dz)

            if (dist > 1.5f) {
                x += (dx / dist) * type.speed * 1.5f * dt
                y += (dy / dist) * type.speed * 1.5f * dt
                z += (dz / dist) * type.speed * 1.5f * dt
                yaw = Math.toDegrees(atan2(dx.toDouble(), dz.toDouble())).toFloat()
            } else {
                // Strike player
                if (player.gameMode != GameMode.CREATIVE) {
                    player.takeDamage(12)
                }
                dragonSwoopTimer = Random.nextFloat() * 6f + 5f
            }
        } else {
            // Circle majestic loop in the sky
            val radius = 18f
            val targetX = player.x + cos(dragonOrbitAngle) * radius
            val targetZ = player.z + sin(dragonOrbitAngle) * radius
            val targetY = player.y + dragonAltitude + sin(dragonOrbitAngle * 2f) * 4f

            val dx = targetX - x
            val dy = targetY - y
            val dz = targetZ - z

            x += dx * 1.5f * dt
            y += dy * 1.2f * dt
            z += dz * 1.5f * dt
            yaw = Math.toDegrees(atan2(-sin(dragonOrbitAngle).toDouble(), cos(dragonOrbitAngle).toDouble())).toFloat()
        }
    }

    fun takeDamage(amount: Int): Boolean {
        health -= amount
        hurtTimer = 0.3f
        if (!isAlive && type.isHostile) {
            com.example.game.advancements.AdvancementManager.trigger(com.example.game.advancements.AdvancementManager.MONSTER_HUNTER)
        }
        return !isAlive
    }
}
