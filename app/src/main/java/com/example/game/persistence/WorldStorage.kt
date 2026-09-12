package com.example.game.persistence

import android.content.Context
import com.example.game.entities.GameMode
import com.example.game.entities.Player
import com.example.game.world.ItemStack
import com.example.game.world.ItemType
import com.example.game.world.World
import org.json.JSONArray
import org.json.JSONObject

data class WorldMetadata(
    val id: String,
    val name: String,
    val seed: Long,
    val gameMode: GameMode,
    val isFlat: Boolean,
    val lastPlayed: Long,
    val version: String = "26.2"
)

class WorldStorage(private val context: Context) {
    private val prefs = context.getSharedPreferences("minecraft_worlds", Context.MODE_PRIVATE)

    fun listWorlds(): List<WorldMetadata> {
        val jsonStr = prefs.getString("world_list", "[]") ?: "[]"
        val list = mutableListOf<WorldMetadata>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    WorldMetadata(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        seed = obj.getLong("seed"),
                        gameMode = GameMode.valueOf(obj.optString("gameMode", "SURVIVAL")),
                        isFlat = obj.optBoolean("isFlat", false),
                        lastPlayed = obj.optLong("lastPlayed", System.currentTimeMillis()),
                        version = obj.optString("version", "26.2")
                    )
                )
            }
        } catch (_: Exception) {}
        if (list.isEmpty()) {
            // Default worlds matching official Java Edition Select World screenshot
            val baseTime = System.currentTimeMillis()
            val defaultWorlds = listOf(
                WorldMetadata(
                    id = "world_new_9",
                    name = "New World",
                    seed = 133742L,
                    gameMode = GameMode.SURVIVAL,
                    isFlat = false,
                    lastPlayed = baseTime - 1000L * 60 * 60 * 24 * 6, // 9/5/26
                    version = "26.2"
                ),
                WorldMetadata(
                    id = "world_new_hardcore",
                    name = "New World",
                    seed = 987654321L,
                    gameMode = GameMode.HARDCORE,
                    isFlat = false,
                    lastPlayed = baseTime - 1000L * 60 * 60 * 24 * 19, // 8/23/26
                    version = "26.2"
                ),
                WorldMetadata(
                    id = "world_new_8",
                    name = "New World",
                    seed = 44556677L,
                    gameMode = GameMode.SURVIVAL,
                    isFlat = false,
                    lastPlayed = baseTime - 1000L * 60 * 60 * 24 * 22, // 8/20/26
                    version = "26.2"
                ),
                WorldMetadata(
                    id = "world_new_7",
                    name = "New World",
                    seed = 99887766L,
                    gameMode = GameMode.SURVIVAL,
                    isFlat = false,
                    lastPlayed = baseTime - 1000L * 60 * 60 * 24 * 26, // 8/16/26
                    version = "26.2"
                )
            )
            list.addAll(defaultWorlds)
            saveWorldList(list)
        }
        return list
    }

    private fun saveWorldList(list: List<WorldMetadata>) {
        val arr = JSONArray()
        for (w in list) {
            val obj = JSONObject().apply {
                put("id", w.id)
                put("name", w.name)
                put("seed", w.seed)
                put("gameMode", w.gameMode.name)
                put("isFlat", w.isFlat)
                put("lastPlayed", w.lastPlayed)
                put("version", w.version)
            }
            arr.put(obj)
        }
        prefs.edit().putString("world_list", arr.toString()).apply()
    }

    fun createWorld(
        name: String,
        seed: Long,
        gameMode: GameMode,
        isFlat: Boolean,
        version: String = "26.2"
    ): WorldMetadata {
        val id = "world_" + System.currentTimeMillis()
        val meta = WorldMetadata(id, name, seed, gameMode, isFlat, System.currentTimeMillis(), version)
        val list = listWorlds().toMutableList()
        list.add(0, meta)
        saveWorldList(list)
        return meta
    }

    fun deleteWorld(id: String) {
        val list = listWorlds().filter { it.id != id }
        saveWorldList(list)
        prefs.edit().remove("data_$id").apply()
    }

    fun saveWorldState(worldMeta: WorldMetadata, world: World, player: Player) {
        try {
            val data = JSONObject().apply {
                put("timeOfDay", world.timeOfDay)
                put("playerX", player.x.toDouble())
                put("playerY", player.y.toDouble())
                put("playerZ", player.z.toDouble())
                put("playerYaw", player.yaw.toDouble())
                put("playerPitch", player.pitch.toDouble())
                put("playerHealth", player.health)
                put("playerHunger", player.hunger)
                put("playerExp", player.expLevel)

                // Save inventory
                val invArr = JSONArray()
                for (slot in player.inventory.indices) {
                    val stack = player.inventory[slot]
                    if (stack != null) {
                        val itemObj = JSONObject().apply {
                            put("slot", slot)
                            put("itemId", stack.item.id)
                            put("count", stack.count)
                        }
                        invArr.put(itemObj)
                    }
                }
                put("inventory", invArr)
            }
            prefs.edit().putString("data_${worldMeta.id}", data.toString()).apply()

            // Update last played
            val list = listWorlds().map {
                if (it.id == worldMeta.id) it.copy(lastPlayed = System.currentTimeMillis()) else it
            }
            saveWorldList(list)
        } catch (_: Exception) {}
    }

    fun loadWorldState(worldMeta: WorldMetadata, world: World, player: Player) {
        val dataStr = prefs.getString("data_${worldMeta.id}", null) ?: return
        try {
            val data = JSONObject(dataStr)
            world.timeOfDay = data.optLong("timeOfDay", 6000L)
            player.x = data.optDouble("playerX", player.x.toDouble()).toFloat()
            player.y = data.optDouble("playerY", player.y.toDouble()).toFloat()
            player.z = data.optDouble("playerZ", player.z.toDouble()).toFloat()
            player.yaw = data.optDouble("playerYaw", player.yaw.toDouble()).toFloat()
            player.pitch = data.optDouble("playerPitch", player.pitch.toDouble()).toFloat()
            player.health = data.optInt("playerHealth", 2000)
            player.hunger = data.optInt("playerHunger", 20)
            player.expLevel = data.optInt("playerExp", 0)

            val invArr = data.optJSONArray("inventory")
            if (invArr != null) {
                player.inventory.fill(null)
                for (i in 0 until invArr.length()) {
                    val itemObj = invArr.getJSONObject(i)
                    val slot = itemObj.getInt("slot")
                    val itemId = itemObj.getString("itemId")
                    val count = itemObj.getInt("count")
                    if (slot in player.inventory.indices) {
                        player.inventory[slot] = ItemStack(ItemType.fromId(itemId), count)
                    }
                }
            }
        } catch (_: Exception) {}
    }
}
