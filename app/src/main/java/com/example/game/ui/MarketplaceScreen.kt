package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.audio.SoundManager
import com.example.game.entities.GameMode

enum class MarketplaceCategory(val displayName: String) {
    ALL("All Free Mods"),
    VERITY_JE("Verity.JE"),
    MODS("All Mods in World"),
    WORLDS("Worlds & Maps"),
    SHADERS("Shaders & Visuals"),
    SKINS("Skins & Models"),
    TEXTURES("Texture Packs")
}

data class MarketplaceItem(
    val id: String,
    val title: String,
    val category: MarketplaceCategory,
    val iconEmoji: String,
    val tag: String,
    val rating: Float,
    val downloads: String,
    val description: String,
    val seed: Long = 0L,
    val gameMode: GameMode = GameMode.SURVIVAL,
    val isFlat: Boolean = false,
    val isWorld: Boolean = false
)

@Composable
fun MarketplaceScreen(
    soundManager: SoundManager,
    onLaunchWorld: (name: String, seed: Long, mode: GameMode, isFlat: Boolean) -> Unit = { _, _, _, _ -> },
    onBack: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(MarketplaceCategory.ALL) }
    val claimedItems = remember { mutableStateListOf<String>("verity_core", "jei") }
    var equippedSkin by remember { mutableStateOf("Diamond Armor Knight") }
    var activeTexturePack by remember { mutableStateOf("Verity.JE Shaderpack") }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    val catalog = remember {
        listOf(
            // Verity.JE Suite
            MarketplaceItem(
                id = "verity_core",
                title = "Verity Java Edition (Verity.JE) Core",
                category = MarketplaceCategory.VERITY_JE,
                iconEmoji = "☕",
                tag = "OFFICIAL VERITY.JE",
                rating = 5.0f,
                downloads = "15.8M",
                description = "Authentic Java Edition parity engine, precise block reach, combat cooldowns, and silky smooth 60+ FPS performance."
            ),
            MarketplaceItem(
                id = "verity_shaders",
                title = "Verity.JE Photorealistic Shaderpack",
                category = MarketplaceCategory.VERITY_JE,
                iconEmoji = "✨",
                tag = "GRAPHICS ENGINE",
                rating = 4.9f,
                downloads = "9.4M",
                description = "Cinematic lighting, god rays, waving sulfur foliage, reflective water surfaces, and soft dynamic shadows."
            ),
            MarketplaceItem(
                id = "verity_hud",
                title = "Verity.JE MiniHUD & Armor Durability",
                category = MarketplaceCategory.VERITY_JE,
                iconEmoji = "🧭",
                tag = "UTILITY HUD",
                rating = 4.9f,
                downloads = "6.1M",
                description = "Display live coordinates, biome radar, tool durability bars, and active status effects in authentic Java style."
            ),
            MarketplaceItem(
                id = "verity_recipe",
                title = "Verity.JE FastCraft Matrix",
                category = MarketplaceCategory.VERITY_JE,
                iconEmoji = "⚡",
                tag = "QOL MODULE",
                rating = 4.8f,
                downloads = "4.2M",
                description = "Quick 1-click crafting matrix for all blocks, tools, weapons, and sulfur equipment."
            ),

            // All Mods in the Whole World
            MarketplaceItem(
                id = "create_mod",
                title = "Create Mod: Kinetic Automations",
                category = MarketplaceCategory.MODS,
                iconEmoji = "⚙️",
                tag = "TECH & KINETICS",
                rating = 5.0f,
                downloads = "22.4M",
                description = "Build rotating gears, water wheels, mechanical conveyor belts, wind mills, automated harvesters, and steam trains."
            ),
            MarketplaceItem(
                id = "jei",
                title = "Just Enough Items (JEI)",
                category = MarketplaceCategory.MODS,
                iconEmoji = "📖",
                tag = "RECIPE VIEWER",
                rating = 4.9f,
                downloads = "35.1M",
                description = "Complete recipe, usage, and drop viewer for every single block, tool, mob drop, and sulfur item in the game."
            ),
            MarketplaceItem(
                id = "sodium_iris",
                title = "Sodium & Iris Shaders Engine",
                category = MarketplaceCategory.SHADERS,
                iconEmoji = "🚀",
                tag = "ULTRA FPS",
                rating = 4.9f,
                downloads = "28.9M",
                description = "Modern OpenGL vertex rendering pipeline delivering up to 300% FPS boosts and seamless shader support."
            ),
            MarketplaceItem(
                id = "twilight_forest",
                title = "The Twilight Forest Dimension",
                category = MarketplaceCategory.MODS,
                iconEmoji = "🌲",
                tag = "BOSS ADVENTURE",
                rating = 4.9f,
                downloads = "18.6M",
                description = "Step through the magical portal into the eternal dusk realm to conquer the Naga, Lich Tower, and Hydra!"
            ),
            MarketplaceItem(
                id = "aether",
                title = "The Aether: Realm of Clouds",
                category = MarketplaceCategory.MODS,
                iconEmoji = "☁️",
                tag = "SKY DIMENSION",
                rating = 4.9f,
                downloads = "16.2M",
                description = "Ascend into the sky kingdom with glowing aerclouds, flying Moas, bronze dungeons, and the Silver Sanctum."
            ),
            MarketplaceItem(
                id = "applied_energistics",
                title = "Applied Energistics 2",
                category = MarketplaceCategory.MODS,
                iconEmoji = "💾",
                tag = "DIGITAL STORAGE",
                rating = 4.8f,
                downloads = "14.1M",
                description = "Convert matter into digital energy stored on ME storage drives, auto-crafting CPUs, and quantum networks."
            ),
            MarketplaceItem(
                id = "biomes_o_plenty",
                title = "Biomes O' Plenty & Sulfur Depths",
                category = MarketplaceCategory.WORLDS,
                iconEmoji = "🌋",
                tag = "WORLD EXPANSION",
                rating = 4.9f,
                downloads = "25.3M",
                description = "70+ new biomes featuring massive subterranean Sulfur Caves, Cherry Blossom Groves, Redwood Forests, and Crag Peaks."
            ),
            MarketplaceItem(
                id = "tinkers_construct",
                title = "Tinkers' Construct",
                category = MarketplaceCategory.MODS,
                iconEmoji = "🔨",
                tag = "CUSTOM WEAPONS",
                rating = 4.9f,
                downloads = "19.7M",
                description = "Construct modular smelteries to melt metals into molten bronze, cobalt, and sulfur alloys to forge indestructible tools."
            ),
            MarketplaceItem(
                id = "alexs_mobs",
                title = "Alex's Mobs: Wildlife Overhaul",
                category = MarketplaceCategory.MODS,
                iconEmoji = "🐻",
                tag = "ANIMALS & MOBS",
                rating = 4.9f,
                downloads = "17.4M",
                description = "80+ exotic realistic mobs including Grizzly Bears, Capybaras, Bald Eagles, Komodo Dragons, and Cave Centipedes."
            ),
            MarketplaceItem(
                id = "physics_mod",
                title = "Physics Mod Pro",
                category = MarketplaceCategory.SHADERS,
                iconEmoji = "💥",
                tag = "PHYSICS ENGINE",
                rating = 4.8f,
                downloads = "11.2M",
                description = "Hyper-realistic block fracture physics, ragdoll mob animations, cloth simulations, and liquid dynamics."
            ),
            MarketplaceItem(
                id = "sulfur_expansion",
                title = "Sulfur Deep Caverns & Cubes Pack",
                category = MarketplaceCategory.WORLDS,
                iconEmoji = "🟡",
                tag = "SULFUR MOD",
                rating = 5.0f,
                downloads = "8.7M",
                description = "Deep sulfur caves packed with explosive sulfur cubes, hydrothermal steam geysers, sulfur crystals, and sulfur bricks."
            ),
            MarketplaceItem(
                id = "journeymap",
                title = "JourneyMap Realtime Radar",
                category = MarketplaceCategory.MODS,
                iconEmoji = "🗺️",
                tag = "MINIMAP & RADAR",
                rating = 4.9f,
                downloads = "31.0M",
                description = "Real-time overhead world map radar, waypoint beacons, mob radar, and underground cave topology."
            ),
            MarketplaceItem(
                id = "optifine_hd",
                title = "OptiFine HD Ultra",
                category = MarketplaceCategory.SHADERS,
                iconEmoji = "🔍",
                tag = "PERFORMANCE & ZOOM",
                rating = 4.9f,
                downloads = "45.0M",
                description = "C-key camera zoom, connected glass textures, dynamic handheld torch illumination, and mipmap filtering."
            ),

            // Worlds
            MarketplaceItem(
                id = "skyblock",
                title = "SkyBlock Classic Remastered",
                category = MarketplaceCategory.WORLDS,
                iconEmoji = "🏝️",
                tag = "SURVIVAL SPAWN",
                rating = 4.9f,
                downloads = "1.2M",
                description = "Survive on a floating voxel sky island with a starter tree and chest. Build a sprawling sky kingdom!",
                seed = 1337420L,
                gameMode = GameMode.SURVIVAL,
                isWorld = true
            ),
            MarketplaceItem(
                id = "sulfur_world_spawn",
                title = "Deep Sulfur Caves Survival",
                category = MarketplaceCategory.WORLDS,
                iconEmoji = "⛏️",
                tag = "SULFUR WORLD",
                rating = 4.9f,
                downloads = "2.3M",
                description = "Spawns deep inside a glowing subterranean sulfur cavern surrounded by sulfur ore, cubes, and crystal clusters!",
                seed = 778899L,
                gameMode = GameMode.SURVIVAL,
                isWorld = true
            )
        )
    }

    val filteredList = remember(selectedCategory) {
        if (selectedCategory == MarketplaceCategory.ALL) {
            catalog
        } else {
            catalog.filter { it.category == selectedCategory }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141414))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF333333), RoundedCornerShape(4.dp))
                            .border(1.5.dp, Color(0xFF666666), RoundedCornerShape(4.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = "Marketplace",
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "MARKETPLACE",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // FREE Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF2E7D32))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "100% FREE",
                            color = Color(0xFFA5D6A7),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Balance indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF262626))
                        .border(1.dp, Color(0xFF444444), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = "Minecoins",
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "0 Coins (FREE)",
                        color = Color(0xFFFFD54F),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Category Filter Tabs
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                items(MarketplaceCategory.values()) { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) Color(0xFF4CAF50) else Color(0xFF2A2A2A))
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFF81C784) else Color(0xFF444444),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable {
                                soundManager.playPop()
                                selectedCategory = category
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = category.displayName,
                            color = if (isSelected) Color.White else Color(0xFFCCCCCC),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Toast status bar if action triggered
            if (toastMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1B5E20))
                        .border(1.dp, Color(0xFF81C784), RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = toastMessage ?: "",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Items List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList, key = { it.id }) { item ->
                    val isClaimed = claimedItems.contains(item.id)
                    val isEquipped = (item.category == MarketplaceCategory.SKINS && equippedSkin == item.title) ||
                            (item.category == MarketplaceCategory.TEXTURES && activeTexturePack == item.title)

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF222222)),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, Color(0xFF3E3E3E), RoundedCornerShape(4.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Emoji Icon Preview Box
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF333333), Color(0xFF1A1A1A))
                                        )
                                    )
                                    .border(1.dp, Color(0xFF555555), RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item.iconEmoji,
                                    fontSize = 30.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Item Metadata & Info
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = item.title,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    // Tag badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(Color(0xFF388E3C).copy(alpha = 0.35f))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = item.tag,
                                            color = Color(0xFF81C784),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                Text(
                                    text = item.description,
                                    color = Color(0xFFAAAAAA),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Rating",
                                            tint = Color(0xFFFFC107),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = item.rating.toString(),
                                            color = Color(0xFFFFD54F),
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Text(
                                        text = "•",
                                        color = Color(0xFF666666),
                                        fontSize = 11.sp
                                    )

                                    Text(
                                        text = "${item.downloads} downloads",
                                        color = Color(0xFF888888),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )

                                    Text(
                                        text = "•",
                                        color = Color(0xFF666666),
                                        fontSize = 11.sp
                                    )

                                    Text(
                                        text = "FREE",
                                        color = Color(0xFF66BB6A),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Action Button
                            if (item.isWorld) {
                                Button(
                                    onClick = {
                                        soundManager.playPop()
                                        if (!claimedItems.contains(item.id)) {
                                            claimedItems.add(item.id)
                                        }
                                        toastMessage = "Launching ${item.title}..."
                                        onLaunchWorld(item.title, item.seed, item.gameMode, item.isFlat)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2E7D32)
                                    ),
                                    shape = RoundedCornerShape(2.dp),
                                    modifier = Modifier
                                        .height(38.dp)
                                        .border(1.5.dp, Color(0xFF81C784), RoundedCornerShape(2.dp))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Download,
                                            contentDescription = "Get World",
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = "Play Free",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            } else {
                                Button(
                                    onClick = {
                                        soundManager.playPop()
                                        if (!claimedItems.contains(item.id)) {
                                            claimedItems.add(item.id)
                                        }
                                        if (item.category == MarketplaceCategory.SKINS) {
                                            equippedSkin = item.title
                                            toastMessage = "Equipped Skin: ${item.title}!"
                                        } else if (item.category == MarketplaceCategory.TEXTURES) {
                                            activeTexturePack = item.title
                                            toastMessage = "Activated Texture Pack: ${item.title}!"
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isEquipped) Color(0xFF388E3C) else Color(0xFF4E4E4E)
                                    ),
                                    shape = RoundedCornerShape(2.dp),
                                    modifier = Modifier
                                        .height(38.dp)
                                        .border(
                                            1.5.dp,
                                            if (isEquipped) Color(0xFF81C784) else Color(0xFF777777),
                                            RoundedCornerShape(2.dp)
                                        )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        if (isEquipped) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Equipped",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "Active",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        } else {
                                            Text(
                                                text = "Get Free",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
