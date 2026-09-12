package com.example.game.ui

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.viewinterop.AndroidView
import com.example.game.audio.SoundManager
import com.example.game.entities.GameMode
import com.example.game.entities.Mob
import com.example.game.entities.MobType
import com.example.game.entities.Player
import com.example.game.world.Biome
import com.example.game.gl.MinecraftRenderer
import com.example.game.gl.VoxelSurfaceView
import com.example.game.persistence.WorldMetadata
import com.example.game.persistence.WorldStorage
import com.example.game.world.BlockType
import com.example.game.world.ItemType
import com.example.game.world.RaycastResult
import com.example.game.world.World
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun GameScreen(
    worldMeta: WorldMetadata,
    storage: WorldStorage,
    soundManager: SoundManager,
    fov: Float,
    onFovChange: (Float) -> Unit,
    sensitivity: Float,
    onSensitivityChange: (Float) -> Unit,
    onExitToTitle: () -> Unit
) {
    val world = remember(worldMeta.id) {
        World(
            seed = worldMeta.seed,
            name = worldMeta.name,
            isFlat = worldMeta.isFlat
        )
    }

    val player = remember(worldMeta.id) {
        val spawn = world.getSpawnPosition()
        Player(x = spawn.first, y = spawn.second, z = spawn.third).apply {
            gameMode = worldMeta.gameMode
        }
    }

    val mobs = remember(worldMeta.id) {
        mutableStateListOf<Mob>().apply {
            // Spawn starter animals
            add(Mob(MobType.COW, player.x + 5f, player.y, player.z + 4f))
            add(Mob(MobType.SHEEP, player.x - 6f, player.y, player.z + 5f))
            add(Mob(MobType.PIG, player.x + 3f, player.y, player.z - 6f))
        }
    }

    var isWorldLoaded by remember { mutableStateOf(false) }
    var worldLoadProgress by remember { mutableFloatStateOf(0f) }

    // Load saved world state if exists and prepare spawn chunks
    LaunchedEffect(worldMeta.id) {
        storage.loadWorldState(worldMeta, world, player)
        val px = (player.x / com.example.game.world.World.CHUNK_SIZE).toInt()
        val pz = (player.z / com.example.game.world.World.CHUNK_SIZE).toInt()
        for (dx in -1..1) {
            for (dz in -1..1) {
                world.getChunk(px + dx, pz + dz)
            }
        }
        for (i in 1..10) {
            kotlinx.coroutines.delay(35)
            worldLoadProgress = i * 0.1f
        }
        isWorldLoaded = true
    }

    val renderer = remember(worldMeta.id) {
        MinecraftRenderer(world, player, mobs).apply {
            this.fovDegrees = fov
        }
    }

    var surfaceViewRef by remember { mutableStateOf<VoxelSurfaceView?>(null) }

    val context = LocalContext.current
    var controlSettings by remember { mutableStateOf(ControlSettings.load(context)) }

    // HUD & Dialog states
    var isF3Visible by remember { mutableStateOf(false) }
    var isInventoryOpen by remember { mutableStateOf(false) }
    var isPauseOpen by remember { mutableStateOf(false) }
    var isChatOpen by remember { mutableStateOf(false) }
    var isCustomControlsOpen by remember { mutableStateOf(false) }
    var isDisconnectConfirmOpen by remember { mutableStateOf(false) }
    var isHardcoreGameOver by remember { mutableStateOf(false) }

    val chatMessages = remember {
        mutableStateListOf(
            "[System] Joined world: ${worldMeta.name}",
            "[System] Minecraft ${worldMeta.version} (Java Edition Official Release)",
            "[System] Welcome! Use D-pad to move, swipe to look around."
        )
    }

    // Movement input states
    var forwardInput by remember { mutableFloatStateOf(0f) }
    var strafeInput by remember { mutableFloatStateOf(0f) }
    var jumpTriggered by remember { mutableStateOf(false) }
    var currentRaycast by remember { mutableStateOf(RaycastResult(hit = false)) }

    // Keep FOV synced with renderer
    LaunchedEffect(fov) {
        renderer.fovDegrees = fov
    }

    // Auto-save on disposal
    DisposableEffect(worldMeta.id) {
        onDispose {
            storage.saveWorldState(worldMeta, world, player)
        }
    }

    // Main 60 Hz Game Loop
    LaunchedEffect(worldMeta.id) {
        var lastTime = System.nanoTime()
        var mobSpawnCooldown = 0f
        var stepSoundCooldown = 0f

        while (true) {
            val now = System.nanoTime()
            val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.005f, 0.05f)
            lastTime = now

            if (!isPauseOpen && !isInventoryOpen) {
                // Update World daylight cycle
                world.tick(dt)

                // Update Player physics
                player.updatePhysics(world, forwardInput, strafeInput, jumpTriggered, dt)
                if (jumpTriggered) jumpTriggered = false

                // Footstep sounds
                if (player.isGrounded && (forwardInput != 0f || strafeInput != 0f)) {
                    stepSoundCooldown -= dt
                    if (stepSoundCooldown <= 0f) {
                        soundManager.playStep()
                        stepSoundCooldown = if (player.isSprinting) 0.28f else 0.42f
                    }
                }

                // Update Mobs
                for (mob in mobs) {
                    mob.update(world, player, dt)
                }

                // Periodically spawn ambient mobs
                mobSpawnCooldown += dt
                if (mobSpawnCooldown > 12f) {
                    mobSpawnCooldown = 0f
                    if (mobs.size < 8) {
                        val angle = Random.nextFloat() * Math.PI.toFloat() * 2f
                        val dist = 14f + Random.nextFloat() * 8f
                        val mx = player.x + sin(angle) * dist
                        val mz = player.z + cos(angle) * dist
                        val currentBiome = world.getBiomeAt(player.x.toInt(), player.z.toInt())
                        val type = if (currentBiome == Biome.SULFUR_CAVES && Random.nextFloat() < 0.6f) {
                            MobType.SULFUR_CUBE
                        } else if (world.isDaytime) {
                            when (Random.nextInt(4)) {
                                0 -> MobType.COW
                                1 -> MobType.SHEEP
                                2 -> MobType.CHICKEN
                                else -> MobType.PIG
                            }
                        } else {
                            when (Random.nextInt(4)) {
                                0 -> MobType.ZOMBIE
                                1 -> MobType.CREEPER
                                2 -> MobType.SKELETON
                                else -> MobType.SULFUR_CUBE
                            }
                        }
                        val spawnY = if (type.isFlyingBoss) player.y + 15f else player.y
                        mobs.add(Mob(type, mx, spawnY, mz))
                    }
                }

                // Void damage check
                if (player.y < -15f && (player.gameMode == GameMode.SURVIVAL || player.gameMode == GameMode.HARDCORE)) {
                    player.takeDamage(4)
                }

                // Death check
                if ((player.gameMode == GameMode.SURVIVAL || player.gameMode == GameMode.HARDCORE) && player.health <= 0) {
                    if (player.gameMode == GameMode.HARDCORE) {
                        isHardcoreGameOver = true
                    } else {
                        // Standard survival respawn
                        val spawn = world.getSpawnPosition()
                        player.x = spawn.first
                        player.y = spawn.second
                        player.z = spawn.third
                        player.health = player.maxHealth
                        player.hunger = player.maxHunger
                        soundManager.playHurt()
                    }
                }

                // Continuous camera raycast
                val yawRad = Math.toRadians(player.yaw.toDouble())
                val pitchRad = Math.toRadians(player.pitch.toDouble())
                val dirX = (-sin(yawRad) * cos(pitchRad)).toFloat()
                val dirY = sin(pitchRad).toFloat()
                val dirZ = (cos(yawRad) * cos(pitchRad)).toFloat()

                val eyeX = player.x
                val eyeY = player.y + player.eyeHeight
                val eyeZ = player.z

                val ray = world.raycast(eyeX, eyeY, eyeZ, dirX, dirY, dirZ, maxDistance = 5.2f)
                currentRaycast = ray
                renderer.targetedRaycast = ray

                // Decay mining progress if player looked away from target block
                if (player.miningProgress > 0f) {
                    if (!ray.hit || ray.blockX != player.miningBlockX || ray.blockY != player.miningBlockY || ray.blockZ != player.miningBlockZ) {
                        player.miningProgress = (player.miningProgress - dt * 2.0f).coerceAtLeast(0f)
                        if (player.miningProgress == 0f) {
                            player.miningBlockX = -1
                            player.miningBlockY = -1
                            player.miningBlockZ = -1
                        }
                    }
                }
            }

            delay(16) // ~60 FPS
        }
    }

    fun breakBlockDirectly(bx: Int, by: Int, bz: Int, block: com.example.game.world.BlockType) {
        world.setBlock(bx, by, bz, com.example.game.world.BlockType.AIR)
        soundManager.playBlockBreak()

        // Trigger Java Advancements
        when (block) {
            com.example.game.world.BlockType.OAK_LOG, com.example.game.world.BlockType.BIRCH_LOG -> com.example.game.advancements.AdvancementManager.trigger(com.example.game.advancements.AdvancementManager.GETTING_WOOD)
            com.example.game.world.BlockType.STONE, com.example.game.world.BlockType.COBBLESTONE -> com.example.game.advancements.AdvancementManager.trigger(com.example.game.advancements.AdvancementManager.STONE_AGE)
            com.example.game.world.BlockType.IRON_ORE -> com.example.game.advancements.AdvancementManager.trigger(com.example.game.advancements.AdvancementManager.ACQUIRE_HARDWARE)
            com.example.game.world.BlockType.DIAMOND_ORE -> com.example.game.advancements.AdvancementManager.trigger(com.example.game.advancements.AdvancementManager.DIAMONDS)
            else -> {}
        }

        // Add drops in Survival & Hardcore
        if (player.gameMode == GameMode.SURVIVAL || player.gameMode == GameMode.HARDCORE) {
            val dropItem = com.example.game.world.ItemType.fromBlock(block)
            player.addItem(dropItem, 1)
        }
    }

    // Block mining handler with survival delay
    fun handleMine() {
        if (player.gameMode == GameMode.SPECTATOR) return
        player.triggerHandSwing()

        // 1. Check entity attack with Java weapon cooldown
        var hitMob = false
        val attackMultiplier = player.attackCooldown.coerceIn(0.2f, 1.0f)
        player.attackCooldown = 0f // Reset cooldown on swing

        for (mob in mobs) {
            if (!mob.isAlive) continue
            val dx = mob.x - player.x
            val dy = mob.y - player.y
            val dz = mob.z - player.z
            val dist = kotlin.math.sqrt(dx * dx + dy * dy + dz * dz)
            if (dist < 3.2f) {
                val baseDmg = player.selectedItemStack?.item?.attackDamage ?: 1
                val totalDmg = kotlin.math.max(1, (baseDmg.toFloat() * attackMultiplier).toInt())
                mob.takeDamage(totalDmg)
                soundManager.playHit()
                player.addExperience(if (mob.isAlive) 1 else 5)
                hitMob = true
                break
            }
        }
        if (hitMob) return

        // 2. Otherwise mine targeted block
        if (currentRaycast.hit) {
            val block = currentRaycast.blockType
            if (block != com.example.game.world.BlockType.AIR && block != com.example.game.world.BlockType.BEDROCK) {
                val bx = currentRaycast.blockX
                val by = currentRaycast.blockY
                val bz = currentRaycast.blockZ

                // Instant breaking in Creative Mode
                if (player.gameMode == GameMode.CREATIVE) {
                    breakBlockDirectly(bx, by, bz, block)
                    return
                }

                // In Survival & Hardcore mode: small delay when breaking blocks!
                if (player.miningBlockX != bx || player.miningBlockY != by || player.miningBlockZ != bz) {
                    player.miningBlockX = bx
                    player.miningBlockY = by
                    player.miningBlockZ = bz
                    player.miningProgress = 0f
                }

                val toolSpeed = player.selectedItemStack?.item?.miningSpeedMultiplier ?: 1.0f
                val hardness = block.hardness.coerceAtLeast(0.6f)
                val breakStep = (0.34f * (toolSpeed / hardness)).coerceIn(0.25f, 0.50f)

                player.miningProgress += breakStep
                soundManager.playHit()

                if (player.miningProgress >= 1.0f) {
                    breakBlockDirectly(bx, by, bz, block)
                    player.miningProgress = 0f
                    player.miningBlockX = -1
                    player.miningBlockY = -1
                    player.miningBlockZ = -1
                }
            }
        }
    }

    // Block placement or food eating handler
    fun handlePlace() {
        if (player.gameMode == GameMode.SPECTATOR) return
        player.triggerHandSwing()

        val selected = player.selectedItemStack
        if (selected != null && selected.item.isFood && (player.health < player.maxHealth || player.hunger < player.maxHunger)) {
            // Eat food!
            player.feed(selected.item.foodRestoration)
            player.heal(selected.item.foodRestoration / 2)
            soundManager.playEat()
            if (player.gameMode == GameMode.SURVIVAL || player.gameMode == GameMode.HARDCORE) {
                player.consumeSelectedItem()
            }
            return
        }

        if (currentRaycast.hit) {
            val px = currentRaycast.placeX
            val py = currentRaycast.placeY
            val pz = currentRaycast.placeZ

            if (!world.isValid(px, py, pz)) return

            // Check collision with player AABB
            val pMinX = floor(player.x - player.width / 2f).toInt()
            val pMaxX = floor(player.x + player.width / 2f).toInt()
            val pMinY = floor(player.y).toInt()
            val pMaxY = floor(player.y + player.height).toInt()
            val pMinZ = floor(player.z - player.width / 2f).toInt()
            val pMaxZ = floor(player.z + player.width / 2f).toInt()

            if (px in pMinX..pMaxX && py in pMinY..pMaxY && pz in pMinZ..pMaxZ) {
                // Can't place inside player!
                return
            }

            val itemToPlace = player.selectedItemStack?.item
            val blockToPlace = itemToPlace?.blockType ?: BlockType.DIRT

            world.setBlock(px, py, pz, blockToPlace)
            soundManager.playBlockPlace()

            if (player.gameMode == GameMode.SURVIVAL || player.gameMode == GameMode.HARDCORE) {
                player.consumeSelectedItem()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 3D OpenGL Voxel View
        AndroidView(
            factory = { ctx ->
                VoxelSurfaceView(ctx, renderer).also { surfaceViewRef = it }
            },
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(sensitivity) {
                    // Right area drag gestures for camera look-around
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val dx = dragAmount.x * 0.22f * sensitivity
                        val dy = dragAmount.y * 0.22f * sensitivity
                        player.yaw = (player.yaw + dx) % 360f
                        player.pitch = (player.pitch - dy).coerceIn(-89f, 89f)
                    }
                }
        )

        // Minecraft HUD Overlay - Controls only appear when the game is loaded
        if (isWorldLoaded) {
            MinecraftHUD(
                player = player,
                mobs = mobs,
                controlSettings = controlSettings,
                onMoveInput = { fwd, str ->
                    forwardInput = fwd
                    strafeInput = str
                },
                onLookDrag = { dx, dy ->
                    player.yaw = (player.yaw + dx * sensitivity) % 360f
                    player.pitch = (player.pitch - dy * sensitivity).coerceIn(-89f, 89f)
                },
                onJump = { jumpTriggered = true },
                onMine = { handleMine() },
                onPlace = { handlePlace() },
                onOpenInventory = { isInventoryOpen = true },
                onOpenPause = { isPauseOpen = true },
                onToggleF3 = { isF3Visible = !isF3Visible },
                onOpenChat = { isChatOpen = true },
                onOpenCustomControls = { isCustomControlsOpen = true },
                onDisconnect = { isDisconnectConfirmOpen = true },
                versionName = worldMeta.version
            )
        } else {
            // Authentic Minecraft "Loading world / Building terrain" screen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF241711)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Loading world...",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Building terrain (Minecraft Java ${worldMeta.version})",
                        color = Color(0xFFAAAAAA),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Box(
                        modifier = Modifier
                            .width(220.dp)
                            .height(14.dp)
                            .background(Color(0xFF111111))
                            .border(2.dp, Color(0xFF666666), RoundedCornerShape(2.dp))
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(worldLoadProgress.coerceIn(0f, 1f))
                                .background(Color(0xFF70E040))
                        )
                    }
                }
            }
        }

        // F3 Debug Screen Overlay
        if (isF3Visible) {
            val verObj = com.example.game.version.MinecraftVersion.fromId(worldMeta.version)
            F3DebugOverlay(
                player = player,
                world = world,
                raycast = currentRaycast,
                versionName = worldMeta.version,
                javaRuntime = verObj.javaRuntime
            )
        }

        // Inventory & Crafting Dialog
        if (isInventoryOpen) {
            InventoryDialog(
                player = player,
                onDismiss = { isInventoryOpen = false },
                onItemCrafted = { soundManager.playPop() }
            )
        }

        // Pause Menu Dialog
        if (isPauseOpen) {
            PauseMenuDialog(
                player = player,
                world = world,
                soundManager = soundManager,
                fov = fov,
                onFovChange = onFovChange,
                sensitivity = sensitivity,
                onSensitivityChange = onSensitivityChange,
                onResume = { isPauseOpen = false },
                onOpenCustomControls = { isCustomControlsOpen = true },
                onSaveAndQuit = {
                    storage.saveWorldState(worldMeta, world, player)
                    onExitToTitle()
                }
            )
        }

        // Chat & Commands Console Dialog
        if (isChatOpen) {
            ChatConsoleDialog(
                player = player,
                world = world,
                mobs = mobs,
                chatMessages = chatMessages,
                onDismiss = { isChatOpen = false }
            )
        }

        // Custom Controls Settings Dialog
        if (isCustomControlsOpen) {
            CustomControlsDialog(
                currentSettings = controlSettings,
                onSaveSettings = { controlSettings = it },
                onDismiss = { isCustomControlsOpen = false }
            )
        }

        // In-Game Disconnect Confirmation Dialog
        if (isDisconnectConfirmOpen) {
            DisconnectConfirmDialog(
                worldName = worldMeta.name,
                onConfirmDisconnect = {
                    storage.saveWorldState(worldMeta, world, player)
                    onExitToTitle()
                },
                onCancel = { isDisconnectConfirmOpen = false }
            )
        }

        // Hardcore Game Over Dialog
        if (isHardcoreGameOver) {
            val score = player.expLevel * 100 + player.inventory.filterNotNull().size * 10
            HardcoreGameOverDialog(
                score = score,
                onSpectate = {
                    player.gameMode = GameMode.SPECTATOR
                    player.health = 20
                    player.hunger = 20
                    isHardcoreGameOver = false
                },
                onTitleScreen = {
                    storage.saveWorldState(worldMeta, world, player)
                    onExitToTitle()
                }
            )
        }
    }
}

@Composable
fun HardcoreGameOverDialog(
    score: Int,
    onSpectate: () -> Unit,
    onTitleScreen: () -> Unit
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false, usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xDA4A0000)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .width(360.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF260808))
                    .border(3.dp, Color(0xFFC62828), RoundedCornerShape(8.dp))
                    .padding(24.dp)
            ) {
                Text(
                    text = "You Died!",
                    color = Color(0xFFFF5252),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Game Over!",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Score: $score",
                    color = Color(0xFFFFD54F),
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Hardcore Mode - You cannot respawn in this world!",
                    color = Color(0xFFFFCDD2),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                MinecraftMenuButton(
                    text = "Spectate World",
                    onClick = onSpectate
                )

                MinecraftMenuButton(
                    text = "Title Screen",
                    onClick = onTitleScreen
                )
            }
        }
    }
}
