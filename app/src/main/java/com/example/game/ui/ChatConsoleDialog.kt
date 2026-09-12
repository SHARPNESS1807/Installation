package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.game.entities.GameMode
import com.example.game.entities.Mob
import com.example.game.entities.MobType
import com.example.game.entities.Player
import com.example.game.world.ItemType
import com.example.game.world.BlockType
import com.example.game.world.World

@Composable
fun ChatConsoleDialog(
    player: Player,
    world: World,
    mobs: MutableList<Mob>,
    chatMessages: MutableList<String>,
    onDismiss: () -> Unit
) {
    var input by remember { mutableStateOf("") }

    fun executeCommand(cmd: String) {
        val trimmed = cmd.trim()
        if (trimmed.isEmpty()) return

        chatMessages.add("> $trimmed")

        if (trimmed.startsWith("/")) {
            val parts = trimmed.substring(1).trim().split("\\s+".toRegex())
            val cmd = parts[0].lowercase()

            fun parseCoord(token: String?, origin: Float): Float {
                if (token == null) return origin
                if (token == "~") return origin
                if (token.startsWith("~")) {
                    val offset = token.substring(1).toFloatOrNull() ?: 0f
                    return origin + offset
                }
                return token.toFloatOrNull() ?: origin
            }

            when (cmd) {
                "summon", "spawn" -> {
                    val rawName = parts.getOrNull(1)?.lowercase()?.removePrefix("minecraft:") ?: "zombie"
                    val sx = parseCoord(parts.getOrNull(2), player.x + 2f)
                    val sy = parseCoord(parts.getOrNull(3), player.y)
                    val sz = parseCoord(parts.getOrNull(4), player.z + 2f)

                    val mobType = MobType.entries.firstOrNull {
                        it.name.lowercase() == rawName ||
                        it.displayName.lowercase().replace(" ", "_") == rawName ||
                        it.displayName.lowercase() == rawName ||
                        it.displayName.lowercase().contains(rawName)
                    } ?: MobType.ZOMBIE

                    mobs.add(Mob(mobType, sx, sy, sz))
                    chatMessages.add("[Server] Summoned new ${mobType.displayName} at (${sx.toInt()}, ${sy.toInt()}, ${sz.toInt()})")
                }
                "gamemode" -> {
                    val mode = parts.getOrNull(1)?.lowercase()
                    when (mode) {
                        "c", "creative", "1" -> {
                            player.gameMode = GameMode.CREATIVE
                            chatMessages.add("[Server] Set own game mode to Creative Mode")
                        }
                        "s", "survival", "0" -> {
                            player.gameMode = GameMode.SURVIVAL
                            chatMessages.add("[Server] Set own game mode to Survival Mode")
                        }
                        "h", "hardcore" -> {
                            player.gameMode = GameMode.HARDCORE
                            chatMessages.add("[Server] Set own game mode to Hardcore Mode")
                        }
                        "sp", "spectator", "3" -> {
                            player.gameMode = GameMode.SPECTATOR
                            chatMessages.add("[Server] Set own game mode to Spectator Mode")
                        }
                        else -> {
                            chatMessages.add("[Server] Usage: /gamemode <survival|creative|spectator|hardcore>")
                        }
                    }
                }
                "defaultgamemode" -> {
                    val mode = parts.getOrNull(1)?.lowercase() ?: "survival"
                    chatMessages.add("[Server] The default game mode is now $mode")
                }
                "time" -> {
                    val sub = parts.getOrNull(1)?.lowercase()
                    if (sub == "set" || sub == "add") {
                        val target = parts.getOrNull(2)?.lowercase()
                        when (target) {
                            "day" -> {
                                world.timeOfDay = 6000L
                                chatMessages.add("[Server] Set the time to 6000 (day)")
                            }
                            "noon" -> {
                                world.timeOfDay = 6000L
                                chatMessages.add("[Server] Set the time to 6000 (noon)")
                            }
                            "night" -> {
                                world.timeOfDay = 18000L
                                chatMessages.add("[Server] Set the time to 18000 (night)")
                            }
                            "midnight" -> {
                                world.timeOfDay = 18000L
                                chatMessages.add("[Server] Set the time to 18000 (midnight)")
                            }
                            else -> {
                                val ticks = target?.toLongOrNull() ?: 6000L
                                if (sub == "add") {
                                    world.timeOfDay = (world.timeOfDay + ticks) % 24000L
                                    chatMessages.add("[Server] Added $ticks to the time")
                                } else {
                                    world.timeOfDay = ticks % 24000L
                                    chatMessages.add("[Server] Set the time to $ticks")
                                }
                            }
                        }
                    } else if (sub == "query") {
                        chatMessages.add("[Server] The time is ${world.timeOfDay}")
                    } else {
                        chatMessages.add("[Server] Usage: /time <set|add|query> <day|night|ticks>")
                    }
                }
                "weather" -> {
                    val type = parts.getOrNull(1)?.lowercase() ?: "clear"
                    chatMessages.add("[Server] Set the weather to $type")
                }
                "give" -> {
                    val rawItem = parts.getOrNull(1)?.lowercase()?.removePrefix("minecraft:") ?: "diamond"
                    val count = (parts.getOrNull(2)?.toIntOrNull() ?: 64).coerceIn(1, 64)
                    val match = ItemType.entries.firstOrNull {
                        it.name.lowercase() == rawItem ||
                        it.id.lowercase() == rawItem ||
                        it.displayName.lowercase().replace(" ", "_") == rawItem ||
                        it.id.lowercase().contains(rawItem) ||
                        it.name.lowercase().contains(rawItem)
                    } ?: ItemType.DIAMOND
                    player.addItem(match, count)
                    chatMessages.add("[Server] Gave $count [${match.displayName}] to Player")
                }
                "tp", "teleport" -> {
                    val tx = parseCoord(parts.getOrNull(1), player.x)
                    val ty = parseCoord(parts.getOrNull(2), player.y)
                    val tz = parseCoord(parts.getOrNull(3), player.z)
                    player.x = tx
                    player.y = ty
                    player.z = tz
                    chatMessages.add("[Server] Teleported Player to ${tx.toInt()}, ${ty.toInt()}, ${tz.toInt()}")
                }
                "kill" -> {
                    val target = parts.getOrNull(1)?.lowercase() ?: "@s"
                    when (target) {
                        "@e", "@e[type=!player]", "mobs" -> {
                            val count = mobs.size
                            mobs.clear()
                            chatMessages.add("[Server] Killed $count entities")
                        }
                        "@s", "@p", "@a", "player" -> {
                            player.health = 0
                            player.hurtTimer = 0.5f
                            chatMessages.add("[Server] Killed Player")
                        }
                        else -> {
                            val count = mobs.count { it.type.displayName.lowercase().contains(target) }
                            mobs.removeAll { it.type.displayName.lowercase().contains(target) }
                            chatMessages.add("[Server] Killed $count $target entities")
                        }
                    }
                }
                "clear" -> {
                    player.inventory.fill(null)
                    chatMessages.add("[Server] Cleared the inventory of Player")
                }
                "setblock" -> {
                    if (parts.size >= 5) {
                        val bx = parseCoord(parts[1], player.x).toInt()
                        val by = parseCoord(parts[2], player.y).toInt()
                        val bz = parseCoord(parts[3], player.z).toInt()
                        val rawBlock = parts[4].lowercase().removePrefix("minecraft:")
                        val bType = BlockType.entries.firstOrNull {
                            it.name.lowercase() == rawBlock ||
                            it.displayName.lowercase().replace(" ", "_") == rawBlock ||
                            it.name.lowercase().contains(rawBlock)
                        } ?: BlockType.STONE
                        world.setBlock(bx, by, bz, bType)
                        chatMessages.add("[Server] Changed the block at $bx, $by, $bz to ${bType.displayName}")
                    } else {
                        chatMessages.add("[Server] Usage: /setblock <x> <y> <z> <block>")
                    }
                }
                "fill" -> {
                    if (parts.size >= 8) {
                        val x1 = parseCoord(parts[1], player.x).toInt()
                        val y1 = parseCoord(parts[2], player.y).toInt().coerceIn(1, 62)
                        val z1 = parseCoord(parts[3], player.z).toInt()
                        val x2 = parseCoord(parts[4], player.x).toInt()
                        val y2 = parseCoord(parts[5], player.y).toInt().coerceIn(1, 62)
                        val z2 = parseCoord(parts[6], player.z).toInt()
                        val rawBlock = parts[7].lowercase().removePrefix("minecraft:")
                        val bType = BlockType.entries.firstOrNull {
                            it.name.lowercase() == rawBlock || it.name.lowercase().contains(rawBlock)
                        } ?: BlockType.STONE

                        val minX = minOf(x1, x2)
                        val maxX = maxOf(x1, x2).coerceAtMost(minX + 32)
                        val minY = minOf(y1, y2)
                        val maxY = maxOf(y1, y2).coerceAtMost(minY + 16)
                        val minZ = minOf(z1, z2)
                        val maxZ = maxOf(z1, z2).coerceAtMost(minZ + 32)
                        var count = 0
                        for (fx in minX..maxX) {
                            for (fy in minY..maxY) {
                                for (fz in minZ..maxZ) {
                                    world.setBlock(fx, fy, fz, bType)
                                    count++
                                }
                            }
                        }
                        chatMessages.add("[Server] Successfully filled $count blocks with ${bType.displayName}")
                    } else {
                        chatMessages.add("[Server] Usage: /fill <x1> <y1> <z1> <x2> <y2> <z2> <block>")
                    }
                }
                "effect" -> {
                    val sub = parts.getOrNull(1)?.lowercase()
                    if (sub == "clear") {
                        chatMessages.add("[Server] Cleared all effects from Player")
                    } else {
                        val effectName = if (sub == "give") parts.getOrNull(2)?.lowercase() ?: "speed" else sub ?: "speed"
                        val seconds = parts.getOrNull(if (sub == "give") 3 else 2)?.toIntOrNull() ?: 30
                        val amp = parts.getOrNull(if (sub == "give") 4 else 3)?.toIntOrNull() ?: 1
                        when {
                            effectName.contains("speed") -> player.isSprinting = true
                            effectName.contains("heal") || effectName.contains("regen") -> player.health = player.maxHealth
                            effectName.contains("sat") || effectName.contains("feed") -> player.hunger = player.maxHunger
                            effectName.contains("jump") -> player.vy = 8f
                        }
                        chatMessages.add("[Server] Applied effect $effectName ($seconds s, amplifier $amp) to Player")
                    }
                }
                "xp", "experience" -> {
                    val amount = parts.getOrNull(2)?.toIntOrNull() ?: parts.getOrNull(1)?.toIntOrNull() ?: 20
                    player.addExperience(amount)
                    chatMessages.add("[Server] Added $amount experience to Player (Level: ${player.expLevel})")
                }
                "seed" -> {
                    chatMessages.add("[Server] World Seed: [${world.seed}]")
                }
                "locate" -> {
                    val target = parts.getOrNull(2)?.lowercase() ?: parts.getOrNull(1)?.lowercase() ?: "sulfur_caves"
                    chatMessages.add("[Server] The nearest [$target] is located at [${(player.x + 64).toInt()}, 14, ${(player.z + 80).toInt()}]")
                }
                "spawnpoint" -> {
                    val sx = parseCoord(parts.getOrNull(1), player.x).toInt()
                    val sy = parseCoord(parts.getOrNull(2), player.y).toInt()
                    val sz = parseCoord(parts.getOrNull(3), player.z).toInt()
                    chatMessages.add("[Server] Set the spawn point to $sx, $sy, $sz")
                }
                "difficulty" -> {
                    val diff = parts.getOrNull(1)?.lowercase() ?: "normal"
                    chatMessages.add("[Server] Set difficulty to $diff")
                }
                "gamerule" -> {
                    val rule = parts.getOrNull(1) ?: "keepInventory"
                    val value = parts.getOrNull(2) ?: "true"
                    chatMessages.add("[Server] Gamerule $rule has been set to: $value")
                }
                "say" -> {
                    val msg = parts.drop(1).joinToString(" ")
                    chatMessages.add("[Server] $msg")
                }
                "me" -> {
                    val action = parts.drop(1).joinToString(" ")
                    chatMessages.add("* Player $action")
                }
                "tell", "msg", "w" -> {
                    val recipient = parts.getOrNull(1) ?: "Player"
                    val msg = parts.drop(2).joinToString(" ")
                    chatMessages.add("[Server] Whispered to $recipient: $msg")
                }
                "help", "?" -> {
                    chatMessages.add("[Server] --- Minecraft Command System (Java 26.2) ---")
                    chatMessages.add("  /summon <entity> [x y z] - Summon any mob")
                    chatMessages.add("  /tp <x y z> - Teleport coordinates (supports ~)")
                    chatMessages.add("  /gamemode <survival|creative|spectator|hardcore>")
                    chatMessages.add("  /give <item> [count] - Give item to inventory")
                    chatMessages.add("  /time <set|add> <day|night|ticks> - Control daylight")
                    chatMessages.add("  /weather <clear|rain|thunder> - Set weather")
                    chatMessages.add("  /kill [@e|@s|<mob>] - Kill entities or player")
                    chatMessages.add("  /clear - Clear player inventory")
                    chatMessages.add("  /effect give <effect> [seconds] [amplifier]")
                    chatMessages.add("  /setblock <x y z> <block> - Place single block")
                    chatMessages.add("  /fill <x1 y1 z1 x2 y2 z2> <block> - Fill region")
                    chatMessages.add("  /xp <amount> - Add experience levels")
                    chatMessages.add("  /seed - View world generator seed")
                    chatMessages.add("  /locate <biome> - Find coordinates")
                    chatMessages.add("  /spawnpoint [x y z] - Set respawn coords")
                    chatMessages.add("  /gamerule, /difficulty, /say, /me, /msg")
                }
                else -> {
                    chatMessages.add("[Server] Unknown or incomplete command: /${parts[0]}. Type /help for assistance")
                }
            }
        } else {
            chatMessages.add("<Player> $trimmed")
        }

        input = ""
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x90000000))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(Color(0xE01E1E1E))
                    .border(2.dp, Color(0xFF4A4A4A), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .clickable(enabled = false) {}
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Chat messages history
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        reverseLayout = true
                    ) {
                        items(chatMessages.reversed()) { msg ->
                            Text(
                                text = msg,
                                color = if (msg.startsWith(">")) Color(0xFFA5D6A7) else if (msg.startsWith("[Server]")) Color(0xFFFFF59D) else Color.White,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                        }
                    }

                    // Quick Command Chips (Horizontally scrollable)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "/summon zombie" to "+ Zombie",
                            "/summon creeper" to "+ Creeper",
                            "/summon sulfur_cube" to "+ Sulfur Cube",
                            "/summon ender_dragon" to "+ Dragon",
                            "/summon iron_golem" to "+ Golem",
                            "/give diamond 64" to "+ 64 Diamonds",
                            "/give sulfur_crystal 16" to "+ Sulfur Crystals",
                            "/tp ~ ~10 ~" to "TP Up 10",
                            "/time set day" to "Day",
                            "/time set night" to "Night",
                            "/weather clear" to "Clear Weather",
                            "/gamemode creative" to "Creative",
                            "/gamemode survival" to "Survival",
                            "/gamemode spectator" to "Spectator",
                            "/kill @e" to "Kill Mobs",
                            "/effect give speed 60 2" to "Speed Boost",
                            "/clear" to "Clear Inv",
                            "/seed" to "Seed",
                            "/help" to "Help"
                        ).forEach { (cmd, label) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF2C2C2C))
                                    .border(1.dp, Color(0xFF666666), RoundedCornerShape(4.dp))
                                    .clickable { executeCommand(cmd) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = Color(0xFFFFD54F),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Input bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = input,
                            onValueChange = { input = it },
                            placeholder = { Text("Press enter to chat or / for command...", color = Color.Gray, fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { executeCommand(input) }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF81C784),
                                unfocusedBorderColor = Color.Gray
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { executeCommand(input) },
                            modifier = Modifier.background(Color(0xFF2E7D32), RoundedCornerShape(4.dp))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}
