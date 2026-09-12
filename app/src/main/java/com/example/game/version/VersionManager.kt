package com.example.game.version

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Manages the active official Minecraft version profile.
 */
class VersionManager(context: Context) {
    private val prefs = context.getSharedPreferences("minecraft_version_prefs", Context.MODE_PRIVATE)

    var currentVersion: MinecraftVersion by mutableStateOf(loadVersion())
        private set

    private fun loadVersion(): MinecraftVersion {
        val savedId = prefs.getString("selected_version_id", MinecraftVersion.DEFAULT.id) ?: MinecraftVersion.DEFAULT.id
        return MinecraftVersion.fromId(savedId)
    }

    fun setVersion(version: MinecraftVersion) {
        currentVersion = version
        prefs.edit().putString("selected_version_id", version.id).apply()
    }
}
