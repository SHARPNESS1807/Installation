package com.example.game.version

/**
 * Official Minecraft Versions and Release Profiles.
 */
enum class MinecraftVersion(
    val id: String,
    val versionName: String,
    val updateTitle: String,
    val releaseType: String,
    val isLatest: Boolean,
    val releaseYear: String,
    val description: String,
    val features: List<String>,
    val javaRuntime: String,
    val patchNotes: String
) {
    RELEASE_26_2(
        id = "26.2",
        versionName = "26.2",
        updateTitle = "Tricky Trials",
        releaseType = "Official Release",
        isLatest = true,
        releaseYear = "2026",
        description = "The latest official Minecraft Java Edition release 26.2 featuring Trial Chambers, Crafter automated crafting, the Breeze mob, and the heavy Mace weapon.",
        features = listOf(
            "The Crafter: Automated crafting with redstone pulses",
            "Mace: High-damage smash attacks based on fall distance",
            "Wind Charges: Breeze mob projectile physics & propulsion",
            "Trial Chambers: Copper & Tuff procedurally generated structures",
            "Copper Blocks, Tuff blocks, and Copper Bulbs"
        ),
        javaRuntime = "OpenJDK 21.0.5 64-Bit (Official Runtime)",
        patchNotes = "Minecraft Java 26.2 update. Added Crafter, Mace, Wind Charge, Copper Block, and Tuff blocks. Updated Java HUD & debug overlays to official Java 26.2 specs."
    ),
    RELEASE_1_20_4(
        id = "1.20.4",
        versionName = "1.20.4",
        updateTitle = "Trails & Tales",
        releaseType = "Official Release",
        isLatest = false,
        releaseYear = "2023",
        description = "Official release focusing on player expression and world variety with Cherry Groves, Archeology, Bamboo sets, and Sniffers.",
        features = listOf(
            "Cherry Blossom Wood & Cherry Groves",
            "Archeology: Brushing suspicious sand and pottery sherds",
            "Armor Trims: Customizing armor with smithing templates",
            "Bamboo Wood Set & Bamboo Rafts",
            "Sniffer prehistoric mob & Torchflower seeds"
        ),
        javaRuntime = "OpenJDK 17.0.8 64-Bit",
        patchNotes = "Introduced Cherry Blossom planks, Archeology, decorated pots, and bamboo mosaics."
    ),
    RELEASE_1_19_4(
        id = "1.19.4",
        versionName = "1.19.4",
        updateTitle = "The Wild Update",
        releaseType = "Official Release",
        isLatest = false,
        releaseYear = "2022",
        description = "Official update adding the terrifying Deep Dark biome, Ancient Cities, the Warden, and Mangrove Swamps.",
        features = listOf(
            "Deep Dark Biome & Ancient Cities",
            "The Warden & Sculk Shriekers",
            "Mangrove Swamp & Mud Bricks",
            "Allay helpful mob",
            "Boat with Chest"
        ),
        javaRuntime = "OpenJDK 17.0.3 64-Bit",
        patchNotes = "Added Sculk blocks, Mud Bricks, Mangrove trees, and Warden sonic boom attack."
    ),
    RELEASE_1_16_5(
        id = "1.16.5",
        versionName = "1.16.5",
        updateTitle = "Nether Update",
        releaseType = "Official Release",
        isLatest = false,
        releaseYear = "2021",
        description = "Iconic official update overhauling the Nether with Netherite, Piglin bartering, and diverse biomes.",
        features = listOf(
            "Netherite Ingot, Sword, and Pickaxe tier",
            "Piglin Bartering & Bastion Remnants",
            "Crimson & Warped Forests",
            "Soul Sand Valley & Basalt Deltas",
            "Respawn Anchor & Lodestone"
        ),
        javaRuntime = "OpenJDK 16.0.1 64-Bit",
        patchNotes = "Netherite gear added above Diamond. New biomes: Warped & Crimson forests."
    ),
    RELEASE_1_12_2(
        id = "1.12.2",
        versionName = "1.12.2",
        updateTitle = "World of Color",
        releaseType = "Official Release",
        isLatest = false,
        releaseYear = "2017",
        description = "The gold standard classic release of Minecraft Java Edition with vivid block colors and high stability.",
        features = listOf(
            "Concrete & Concrete Powder",
            "16 Glazed Terracotta patterns",
            "Parrot pets & Illusioner mob",
            "Classic recipe book UI",
            "Classic 1.8-style combat pacing"
        ),
        javaRuntime = "Java 8 Update 51 64-Bit",
        patchNotes = "Vibrant palette rework with Concrete and Glazed Terracotta blocks."
    ),
    RELEASE_BETA_1_7_3(
        id = "b1.7.3",
        versionName = "Beta 1.7.3",
        updateTitle = "Adventure Pre-Release",
        releaseType = "Official Beta",
        isLatest = false,
        releaseYear = "2011",
        description = "Nostalgic official beta version prior to the Adventure Update. Features vintage terrain generation and retro mechanics.",
        features = listOf(
            "Vintage world generator with towering terrain",
            "No hunger bar (food restores health directly)",
            "Instant TNT activation with left click",
            "Retro Minecraft audio and fog lighting"
        ),
        javaRuntime = "Java 6 Update 26 64-Bit",
        patchNotes = "Classic Minecraft mechanics, shears, clay generation, and piston mechanics."
    );

    val fullDisplayName: String get() = "Minecraft $versionName ($updateTitle)"

    companion object {
        val DEFAULT = RELEASE_26_2

        fun fromId(id: String): MinecraftVersion {
            return entries.firstOrNull { it.id == id || it.versionName == id } ?: DEFAULT
        }
    }
}
