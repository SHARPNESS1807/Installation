package com.example.game.ui

import android.content.Context

enum class ControlStyle {
    DPAD,
    JOYSTICK
}

data class ControlSettings(
    val dpadScale: Float = 1.0f,               // 0.7f to 1.5f
    val buttonScale: Float = 1.0f,             // 0.7f to 1.5f
    val opacity: Float = 0.85f,                // 0.3f to 1.0f
    val isLeftHanded: Boolean = false,         // Swap movement and action buttons
    val controlStyle: ControlStyle = ControlStyle.DPAD,
    val showInGameDisconnect: Boolean = true,  // Render disconnect button directly in HUD
    val dpadOffsetX: Float = 0f,               // -40dp to +40dp
    val dpadOffsetY: Float = 0f,               // -40dp to +40dp
    val actionsOffsetX: Float = 0f,            // -40dp to +40dp
    val actionsOffsetY: Float = 0f,            // -40dp to +40dp
    val hapticFeedback: Boolean = true
) {
    companion object {
        private const val PREFS_NAME = "minecraft_controls_prefs"

        fun load(context: Context): ControlSettings {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return ControlSettings(
                dpadScale = prefs.getFloat("dpadScale", 1.0f),
                buttonScale = prefs.getFloat("buttonScale", 1.0f),
                opacity = prefs.getFloat("opacity", 0.85f),
                isLeftHanded = prefs.getBoolean("isLeftHanded", false),
                controlStyle = try {
                    ControlStyle.valueOf(
                        prefs.getString("controlStyle", ControlStyle.DPAD.name) ?: ControlStyle.DPAD.name
                    )
                } catch (_: Exception) {
                    ControlStyle.DPAD
                },
                showInGameDisconnect = prefs.getBoolean("showInGameDisconnect", true),
                dpadOffsetX = prefs.getFloat("dpadOffsetX", 0f),
                dpadOffsetY = prefs.getFloat("dpadOffsetY", 0f),
                actionsOffsetX = prefs.getFloat("actionsOffsetX", 0f),
                actionsOffsetY = prefs.getFloat("actionsOffsetY", 0f),
                hapticFeedback = prefs.getBoolean("hapticFeedback", true)
            )
        }

        fun save(context: Context, settings: ControlSettings) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putFloat("dpadScale", settings.dpadScale)
                .putFloat("buttonScale", settings.buttonScale)
                .putFloat("opacity", settings.opacity)
                .putBoolean("isLeftHanded", settings.isLeftHanded)
                .putString("controlStyle", settings.controlStyle.name)
                .putBoolean("showInGameDisconnect", settings.showInGameDisconnect)
                .putFloat("dpadOffsetX", settings.dpadOffsetX)
                .putFloat("dpadOffsetY", settings.dpadOffsetY)
                .putFloat("actionsOffsetX", settings.actionsOffsetX)
                .putFloat("actionsOffsetY", settings.actionsOffsetY)
                .putBoolean("hapticFeedback", settings.hapticFeedback)
                .apply()
        }
    }
}
