package com.example.game.gl

import android.content.Context
import android.opengl.GLSurfaceView

class VoxelSurfaceView(
    context: Context,
    val renderer: MinecraftRenderer
) : GLSurfaceView(context) {

    init {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }
}
