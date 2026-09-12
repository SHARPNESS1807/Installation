package com.example.game.advancements

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.game.audio.SoundManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class Advancement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val category: String = "Minecraft"
) {
    val unlocked: Boolean get() = AdvancementManager.isUnlocked(this)
}

object AdvancementManager {
    val GETTING_WOOD = Advancement("getting_wood", "Getting Wood", "Punch a tree until a block of wood pops out", "🪵")
    val BENCHMARKING = Advancement("benchmarking", "Benchmarking", "Craft a crafting table with wooden planks", "🪓")
    val TIME_TO_MINE = Advancement("time_to_mine", "Time to Mine!", "Use planks and sticks to make a pickaxe", "⛏️")
    val STONE_AGE = Advancement("stone_age", "Stone Age", "Mine stone with your new pickaxe", "🪨")
    val HOT_TOPIC = Advancement("hot_topic", "Hot Topic", "Construct a furnace out of cobblestone", "🔥")
    val ACQUIRE_HARDWARE = Advancement("acquire_hardware", "Acquire Hardware", "Smelt or acquire an iron ingot", "🪙")
    val TIME_TO_STRIKE = Advancement("time_to_strike", "Time to Strike!", "Craft a sword to defend yourself", "🗡️")
    val DIAMONDS = Advancement("diamonds", "Diamonds!", "Acquire diamonds from the deep depths", "💎")
    val NOT_TODAY = Advancement("not_today", "Not Today, Thank You", "Deflect a hit or equip a shield in off-hand", "🛡️")
    val MONSTER_HUNTER = Advancement("monster_hunter", "Monster Hunter", "Attack and defeat a hostile monster", "🧟")
    val ADVENTURING_TIME = Advancement("adventuring_time", "Adventuring Time", "Explore over 200 blocks into the infinite world", "🧭")

    val advancements = listOf(
        GETTING_WOOD,
        BENCHMARKING,
        TIME_TO_MINE,
        STONE_AGE,
        HOT_TOPIC,
        ACQUIRE_HARDWARE,
        TIME_TO_STRIKE,
        DIAMONDS,
        NOT_TODAY,
        MONSTER_HUNTER,
        ADVENTURING_TIME
    )

    private val unlockedIds = mutableSetOf<String>()
    var activeToast by mutableStateOf<Advancement?>(null)
        private set

    private val scope = CoroutineScope(Dispatchers.Main)

    fun isUnlocked(adv: Advancement): Boolean = unlockedIds.contains(adv.id)

    fun trigger(adv: Advancement, soundManager: SoundManager? = null) {
        if (unlockedIds.contains(adv.id)) return
        unlockedIds.add(adv.id)
        activeToast = adv
        soundManager?.playLevelUp()

        scope.launch {
            delay(4000)
            if (activeToast?.id == adv.id) {
                activeToast = null
            }
        }
    }

    fun reset() {
        unlockedIds.clear()
        activeToast = null
    }
}
