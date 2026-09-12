package com.example.game.gl

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.game.entities.Mob
import com.example.game.entities.Player
import com.example.game.world.BlockType
import com.example.game.world.RaycastResult
import com.example.game.world.World
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-performance 3D Voxel Engine OpenGL ES 2.0 Renderer.
 * Implements chunk face-culling, dynamic sky/lighting, first-person hand & tool rendering,
 * mobs, and targeted block wireframes.
 */
class MinecraftRenderer(
    val world: World,
    val player: Player,
    val mobs: MutableList<Mob>
) : GLSurfaceView.Renderer {

    private var programId = 0
    private var aPositionLoc = 0
    private var aTexCoordLoc = 0
    private var aShadeLoc = 0
    private var uMVPMatrixLoc = 0
    private var uTextureLoc = 0
    private var uColorTintLoc = 0
    private var uAmbientLoc = 0
    private var uCameraPosLoc = 0

    // Matrix buffers
    private val modelMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val identityMatrix = FloatArray(16).apply { Matrix.setIdentityM(this, 0) }

    // Chunk VBO / buffer management
    private class ChunkMesh {
        var vertexBuffer: FloatBuffer? = null
        var vertexCount: Int = 0
    }

    private val chunkMeshMap = HashMap<Long, ChunkMesh>()
    private val tempVertexList = ArrayList<Float>(16384)

    // Hand mesh buffer
    private var handVertexBuffer: FloatBuffer? = null
    private var handVertexCount = 0

    // Sun / Moon / Sky buffers
    private var sunMoonVertexBuffer: FloatBuffer? = null

    // Wireframe selection box buffer
    private var boxLineBuffer: FloatBuffer? = null

    var targetedRaycast: RaycastResult = RaycastResult(hit = false)
    var viewWidth: Int = 1
    var viewHeight: Int = 1
    var fovDegrees: Float = 75f

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthFunc(GLES20.GL_LEQUAL)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glCullFace(GLES20.GL_BACK)

        initShaders()
        TextureAtlas.initGL()
        initSkyBuffers()
        initBoxLineBuffer()
        buildHandMesh()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        viewWidth = width
        viewHeight = height
        GLES20.glViewport(0, 0, width, height)
        val aspect = width.toFloat() / height.toFloat().coerceAtLeast(1f)
        Matrix.perspectiveM(projectionMatrix, 0, fovDegrees, aspect, 0.1f, 120.0f)
    }

    override fun onDrawFrame(gl: GL10?) {
        // Sky color based on time of day
        val skyR: Float
        val skyG: Float
        val skyB: Float

        val sunAngle = world.sunAngle // 0..360
        val ambient = world.ambientLight

        if (world.isDaytime) {
            // Day to sunset
            val dayFactor = sin(Math.toRadians(sunAngle.toDouble())).toFloat().coerceIn(0f, 1f)
            skyR = 0.15f * (1f - dayFactor) + 0.52f * dayFactor
            skyG = 0.20f * (1f - dayFactor) + 0.74f * dayFactor
            skyB = 0.35f * (1f - dayFactor) + 1.00f * dayFactor
        } else {
            // Night
            skyR = 0.04f
            skyG = 0.05f
            skyB = 0.12f
        }

        GLES20.glClearColor(skyR, skyG, skyB, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Setup camera View Matrix
        val eyeX = player.x
        val eyeY = player.y + player.eyeHeight
        val eyeZ = player.z

        val yawRad = Math.toRadians(player.yaw.toDouble())
        val pitchRad = Math.toRadians(player.pitch.toDouble())

        val dirX = (-sin(yawRad) * cos(pitchRad)).toFloat()
        val dirY = sin(pitchRad).toFloat()
        val dirZ = (cos(yawRad) * cos(pitchRad)).toFloat()

        val camDist = when (player.cameraPerspective) {
            com.example.game.entities.Player.CameraPerspective.FIRST_PERSON -> 0f
            com.example.game.entities.Player.CameraPerspective.THIRD_PERSON_BACK -> 3.5f
            com.example.game.entities.Player.CameraPerspective.THIRD_PERSON_FRONT -> -3.5f
        }
        val camX = eyeX - dirX * camDist
        val camY = (eyeY - dirY * camDist).coerceAtLeast(player.y + 0.3f)
        val camZ = eyeZ - dirZ * camDist

        if (player.cameraPerspective == com.example.game.entities.Player.CameraPerspective.THIRD_PERSON_FRONT) {
            Matrix.setLookAtM(
                viewMatrix, 0,
                camX, camY, camZ,
                eyeX, eyeY, eyeZ,
                0f, 1f, 0f
            )
        } else {
            Matrix.setLookAtM(
                viewMatrix, 0,
                camX, camY, camZ,
                camX + dirX, camY + dirY, camZ + dirZ,
                0f, 1f, 0f
            )
        }

        GLES20.glUseProgram(programId)
        GLES20.glUniform1i(uTextureLoc, 0)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, TextureAtlas.textureId)
        GLES20.glUniform1f(uAmbientLoc, ambient)
        GLES20.glUniform3f(uCameraPosLoc, eyeX, eyeY, eyeZ)

        // 1. Draw Celestial bodies (Sun & Moon)
        drawSunAndMoon(eyeX, eyeY, eyeZ, sunAngle)

        // 2. Dynamic Infinite Chunk Loading & Meshing around player
        val renderRadius = 3
        world.updateChunksAround(eyeX, eyeZ, renderRadius)

        val playerCx = kotlin.math.floor(eyeX.toDouble() / World.CHUNK_SIZE).toInt()
        val playerCz = kotlin.math.floor(eyeZ.toDouble() / World.CHUNK_SIZE).toInt()

        // Clean up unused meshes
        val maxDistSq = (renderRadius + 2) * (renderRadius + 2)
        val meshIter = chunkMeshMap.entries.iterator()
        while (meshIter.hasNext()) {
            val entry = meshIter.next()
            val mcx = com.example.game.world.Chunk.unpackX(entry.key)
            val mcz = com.example.game.world.Chunk.unpackZ(entry.key)
            val dSq = (mcx - playerCx) * (mcx - playerCx) + (mcz - playerCz) * (mcz - playerCz)
            if (dSq > maxDistSq) {
                meshIter.remove()
            }
        }

        // Rebuild dirty meshes in active radius
        for (cx in (playerCx - renderRadius)..(playerCx + renderRadius)) {
            for (cz in (playerCz - renderRadius)..(playerCz + renderRadius)) {
                val chunk = world.getOrCreateChunk(cx, cz)
                val key = com.example.game.world.Chunk.packKey(cx, cz)
                var mesh = chunkMeshMap[key]
                if (mesh == null) {
                    mesh = ChunkMesh()
                    chunkMeshMap[key] = mesh
                }
                if (chunk.isDirty || mesh.vertexBuffer == null) {
                    buildChunkMesh(cx, cz, mesh)
                    chunk.isDirty = false
                }
            }
        }

        // 3. Render World Chunks
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(uMVPMatrixLoc, 1, false, mvpMatrix, 0)
        GLES20.glUniform4f(uColorTintLoc, 1f, 1f, 1f, 1f)

        // Render opaque chunk blocks
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glDisable(GLES20.GL_BLEND)

        for (mesh in chunkMeshMap.values) {
            val vb = mesh.vertexBuffer ?: continue
            if (mesh.vertexCount == 0) continue

            vb.position(0)
            GLES20.glVertexAttribPointer(aPositionLoc, 3, GLES20.GL_FLOAT, false, 6 * 4, vb)
            GLES20.glEnableVertexAttribArray(aPositionLoc)

            vb.position(3)
            GLES20.glVertexAttribPointer(aTexCoordLoc, 2, GLES20.GL_FLOAT, false, 6 * 4, vb)
            GLES20.glEnableVertexAttribArray(aTexCoordLoc)

            vb.position(5)
            GLES20.glVertexAttribPointer(aShadeLoc, 1, GLES20.GL_FLOAT, false, 6 * 4, vb)
            GLES20.glEnableVertexAttribArray(aShadeLoc)

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mesh.vertexCount)
        }

        // 4. Render 3D Mobs
        renderMobs()

        // 5. Render Targeted Block Wireframe Box & Breaking Cracks
        renderTargetedBlock()

        // 6. Render First-Person Hand & Held Item
        renderPlayerHand()
    }

    private fun drawSunAndMoon(eyeX: Float, eyeY: Float, eyeZ: Float, sunAngle: Float) {
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE)

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, eyeX, eyeY, eyeZ)
        Matrix.rotateM(modelMatrix, 0, sunAngle, 0f, 0f, 1f)

        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(uMVPMatrixLoc, 1, false, mvpMatrix, 0)

        // Sun
        GLES20.glUniform4f(uColorTintLoc, 1.0f, 0.95f, 0.4f, 1.0f)
        sunMoonVertexBuffer?.let { vb ->
            vb.position(0)
            GLES20.glVertexAttribPointer(aPositionLoc, 3, GLES20.GL_FLOAT, false, 6 * 4, vb)
            GLES20.glEnableVertexAttribArray(aPositionLoc)

            vb.position(3)
            GLES20.glVertexAttribPointer(aTexCoordLoc, 2, GLES20.GL_FLOAT, false, 6 * 4, vb)
            GLES20.glEnableVertexAttribArray(aTexCoordLoc)

            vb.position(5)
            GLES20.glVertexAttribPointer(aShadeLoc, 1, GLES20.GL_FLOAT, false, 6 * 4, vb)
            GLES20.glEnableVertexAttribArray(aShadeLoc)

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6)
        }

        GLES20.glDisable(GLES20.GL_BLEND)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
    }

    private fun renderMobs() {
        for (mob in mobs) {
            if (!mob.isAlive) continue

            val scale = mob.type.scale

            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, mob.x, mob.y, mob.z)
            Matrix.rotateM(modelMatrix, 0, -mob.yaw, 0f, 1f, 0f)
            Matrix.scaleM(modelMatrix, 0, scale, scale, scale)

            Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)
            GLES20.glUniformMatrix4fv(uMVPMatrixLoc, 1, false, mvpMatrix, 0)

            if (mob.hurtTimer > 0) {
                GLES20.glUniform4f(uColorTintLoc, 1.0f, 0.3f, 0.3f, 1.0f)
            } else {
                GLES20.glUniform4f(uColorTintLoc, mob.type.bodyColorR, mob.type.bodyColorG, mob.type.bodyColorB, 1.0f)
            }

            // Draw mob main body
            handVertexBuffer?.let { vb ->
                vb.position(0)
                GLES20.glVertexAttribPointer(aPositionLoc, 3, GLES20.GL_FLOAT, false, 6 * 4, vb)
                GLES20.glEnableVertexAttribArray(aPositionLoc)

                vb.position(3)
                GLES20.glVertexAttribPointer(aTexCoordLoc, 2, GLES20.GL_FLOAT, false, 6 * 4, vb)
                GLES20.glEnableVertexAttribArray(aTexCoordLoc)

                vb.position(5)
                GLES20.glVertexAttribPointer(aShadeLoc, 1, GLES20.GL_FLOAT, false, 6 * 4, vb)
                GLES20.glEnableVertexAttribArray(aShadeLoc)

                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, handVertexCount)
            }

            // If Ender Dragon, draw flapping wings & head
            if (mob.type == com.example.game.entities.MobType.ENDER_DRAGON) {
                val flapAngle = kotlin.math.sin(mob.wingFlapTimer) * 28f

                // Left Wing
                Matrix.setIdentityM(modelMatrix, 0)
                Matrix.translateM(modelMatrix, 0, mob.x, mob.y + 0.5f * scale, mob.z)
                Matrix.rotateM(modelMatrix, 0, -mob.yaw, 0f, 1f, 0f)
                Matrix.translateM(modelMatrix, 0, -0.6f * scale, 0f, 0f)
                Matrix.rotateM(modelMatrix, 0, flapAngle, 0f, 0f, 1f)
                Matrix.scaleM(modelMatrix, 0, 1.6f * scale, 0.08f * scale, 0.8f * scale)
                Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
                Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)
                GLES20.glUniformMatrix4fv(uMVPMatrixLoc, 1, false, mvpMatrix, 0)
                GLES20.glUniform4f(uColorTintLoc, 0.18f, 0.08f, 0.22f, 1.0f)
                handVertexBuffer?.let { GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, handVertexCount) }

                // Right Wing
                Matrix.setIdentityM(modelMatrix, 0)
                Matrix.translateM(modelMatrix, 0, mob.x, mob.y + 0.5f * scale, mob.z)
                Matrix.rotateM(modelMatrix, 0, -mob.yaw, 0f, 1f, 0f)
                Matrix.translateM(modelMatrix, 0, 0.6f * scale, 0f, 0f)
                Matrix.rotateM(modelMatrix, 0, -flapAngle, 0f, 0f, 1f)
                Matrix.scaleM(modelMatrix, 0, 1.6f * scale, 0.08f * scale, 0.8f * scale)
                Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
                Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)
                GLES20.glUniformMatrix4fv(uMVPMatrixLoc, 1, false, mvpMatrix, 0)
                GLES20.glUniform4f(uColorTintLoc, 0.18f, 0.08f, 0.22f, 1.0f)
                handVertexBuffer?.let { GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, handVertexCount) }

                // Glowing Purple Eyes
                Matrix.setIdentityM(modelMatrix, 0)
                Matrix.translateM(modelMatrix, 0, mob.x, mob.y + 0.2f * scale, mob.z)
                Matrix.rotateM(modelMatrix, 0, -mob.yaw, 0f, 1f, 0f)
                Matrix.translateM(modelMatrix, 0, 0f, 0f, 0.6f * scale)
                Matrix.scaleM(modelMatrix, 0, 0.35f * scale, 0.2f * scale, 0.35f * scale)
                Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
                Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)
                GLES20.glUniformMatrix4fv(uMVPMatrixLoc, 1, false, mvpMatrix, 0)
                GLES20.glUniform4f(uColorTintLoc, 0.8f, 0.1f, 0.95f, 1.0f)
                handVertexBuffer?.let { GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, handVertexCount) }
            }
        }
    }

    private fun renderTargetedBlock() {
        if (!targetedRaycast.hit) return

        val bx = targetedRaycast.blockX.toFloat()
        val by = targetedRaycast.blockY.toFloat()
        val bz = targetedRaycast.blockZ.toFloat()

        // Draw wireframe selection outline
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, bx, by, bz)
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(uMVPMatrixLoc, 1, false, mvpMatrix, 0)

        GLES20.glUniform4f(uColorTintLoc, 0.1f, 0.1f, 0.1f, 1.0f)
        boxLineBuffer?.let { lb ->
            lb.position(0)
            GLES20.glVertexAttribPointer(aPositionLoc, 3, GLES20.GL_FLOAT, false, 3 * 4, lb)
            GLES20.glEnableVertexAttribArray(aPositionLoc)
            GLES20.glDisableVertexAttribArray(aTexCoordLoc)
            GLES20.glVertexAttrib1f(aShadeLoc, 1.0f)

            GLES20.glLineWidth(3.0f)
            GLES20.glDrawArrays(GLES20.GL_LINES, 0, 24)
        }
    }

    private fun renderPlayerHand() {
        // Render hand in screen-space view
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT)

        val swing = player.handSwingTimer // 0.25..0
        val swingProgress = if (swing > 0) sin((0.25f - swing) / 0.25f * Math.PI).toFloat() else 0f

        Matrix.setIdentityM(modelMatrix, 0)
        // Position hand in lower-right
        Matrix.translateM(modelMatrix, 0, 0.35f - swingProgress * 0.15f, -0.32f + swingProgress * 0.1f, -0.6f - swingProgress * 0.15f)
        Matrix.rotateM(modelMatrix, 0, -25f - swingProgress * 45f, 1f, 0f, 0f)
        Matrix.rotateM(modelMatrix, 0, 35f, 0f, 1f, 0f)
        Matrix.scaleM(modelMatrix, 0, 0.22f, 0.5f, 0.22f)

        // Use custom projection for HUD hand
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, modelMatrix, 0)
        GLES20.glUniformMatrix4fv(uMVPMatrixLoc, 1, false, mvpMatrix, 0)

        // Hand tint (skin tone or held item color)
        val held = player.selectedItemStack
        if (held?.item?.isTool == true) {
            GLES20.glUniform4f(uColorTintLoc, 0.85f, 0.85f, 0.9f, 1.0f)
        } else if (held?.item?.blockType != null) {
            GLES20.glUniform4f(uColorTintLoc, 1.0f, 1.0f, 1.0f, 1.0f)
        } else {
            // Steve skin color
            GLES20.glUniform4f(uColorTintLoc, 0.82f, 0.58f, 0.45f, 1.0f)
        }

        handVertexBuffer?.let { vb ->
            vb.position(0)
            GLES20.glVertexAttribPointer(aPositionLoc, 3, GLES20.GL_FLOAT, false, 6 * 4, vb)
            GLES20.glEnableVertexAttribArray(aPositionLoc)

            vb.position(3)
            GLES20.glVertexAttribPointer(aTexCoordLoc, 2, GLES20.GL_FLOAT, false, 6 * 4, vb)
            GLES20.glEnableVertexAttribArray(aTexCoordLoc)

            vb.position(5)
            GLES20.glVertexAttribPointer(aShadeLoc, 1, GLES20.GL_FLOAT, false, 6 * 4, vb)
            GLES20.glEnableVertexAttribArray(aShadeLoc)

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, handVertexCount)
        }
    }

    private fun buildChunkMesh(cx: Int, cz: Int, mesh: ChunkMesh) {
        tempVertexList.clear()

        val startX = cx * World.CHUNK_SIZE
        val endX = startX + World.CHUNK_SIZE
        val startZ = cz * World.CHUNK_SIZE
        val endZ = startZ + World.CHUNK_SIZE

        for (x in startX until endX) {
            for (y in 0 until World.CHUNK_HEIGHT) {
                for (z in startZ until endZ) {
                    val block = world.getBlock(x, y, z)
                    if (block == BlockType.AIR) continue

                    // Check all 6 faces for face culling
                    // 1. Top (+Y)
                    val topBlock = world.getBlock(x, y + 1, z)
                    if (topBlock == BlockType.AIR || (topBlock.isTransparent && topBlock != block)) {
                        addFace(x, y, z, 0, block.topTex, 1.0f)
                    }

                    // 2. Bottom (-Y)
                    val botBlock = world.getBlock(x, y - 1, z)
                    if (y > 0 && (botBlock == BlockType.AIR || (botBlock.isTransparent && botBlock != block))) {
                        addFace(x, y, z, 1, block.bottomTex, 0.55f)
                    }

                    // 3. North (-Z)
                    val northBlock = world.getBlock(x, y, z - 1)
                    if (northBlock == BlockType.AIR || (northBlock.isTransparent && northBlock != block)) {
                        addFace(x, y, z, 2, block.sideTex, 0.72f)
                    }

                    // 4. South (+Z)
                    val southBlock = world.getBlock(x, y, z + 1)
                    if (southBlock == BlockType.AIR || (southBlock.isTransparent && southBlock != block)) {
                        addFace(x, y, z, 3, block.sideTex, 0.72f)
                    }

                    // 5. West (-X)
                    val westBlock = world.getBlock(x - 1, y, z)
                    if (westBlock == BlockType.AIR || (westBlock.isTransparent && westBlock != block)) {
                        addFace(x, y, z, 4, block.sideTex, 0.85f)
                    }

                    // 6. East (+X)
                    val eastBlock = world.getBlock(x + 1, y, z)
                    if (eastBlock == BlockType.AIR || (eastBlock.isTransparent && eastBlock != block)) {
                        addFace(x, y, z, 5, block.sideTex, 0.85f)
                    }
                }
            }
        }

        val totalVertices = tempVertexList.size / 6

        if (totalVertices > 0) {
            val byteBuffer = ByteBuffer.allocateDirect(tempVertexList.size * 4).order(ByteOrder.nativeOrder())
            val floatBuffer = byteBuffer.asFloatBuffer()
            val arr = FloatArray(tempVertexList.size)
            for (i in tempVertexList.indices) arr[i] = tempVertexList[i]
            floatBuffer.put(arr)
            floatBuffer.position(0)
            mesh.vertexBuffer = floatBuffer
            mesh.vertexCount = totalVertices
        } else {
            mesh.vertexBuffer = null
            mesh.vertexCount = 0
        }
    }

    private fun addFace(x: Int, y: Int, z: Int, face: Int, texIdx: Int, shade: Float) {
        val uv = TextureAtlas.getUV(texIdx)
        val u0 = uv[0]
        val v0 = uv[1]
        val u1 = uv[2]
        val v1 = uv[3]

        val fx = x.toFloat()
        val fy = y.toFloat()
        val fz = z.toFloat()

        when (face) {
            0 -> { // Top (+Y)
                putVertex(fx, fy + 1f, fz + 1f, u0, v1, shade)
                putVertex(fx + 1f, fy + 1f, fz + 1f, u1, v1, shade)
                putVertex(fx + 1f, fy + 1f, fz, u1, v0, shade)
                putVertex(fx, fy + 1f, fz + 1f, u0, v1, shade)
                putVertex(fx + 1f, fy + 1f, fz, u1, v0, shade)
                putVertex(fx, fy + 1f, fz, u0, v0, shade)
            }
            1 -> { // Bottom (-Y)
                putVertex(fx, fy, fz, u0, v0, shade)
                putVertex(fx + 1f, fy, fz, u1, v0, shade)
                putVertex(fx + 1f, fy, fz + 1f, u1, v1, shade)
                putVertex(fx, fy, fz, u0, v0, shade)
                putVertex(fx + 1f, fy, fz + 1f, u1, v1, shade)
                putVertex(fx, fy, fz + 1f, u0, v1, shade)
            }
            2 -> { // North (-Z)
                putVertex(fx + 1f, fy, fz, u0, v1, shade)
                putVertex(fx, fy, fz, u1, v1, shade)
                putVertex(fx, fy + 1f, fz, u1, v0, shade)
                putVertex(fx + 1f, fy, fz, u0, v1, shade)
                putVertex(fx, fy + 1f, fz, u1, v0, shade)
                putVertex(fx + 1f, fy + 1f, fz, u0, v0, shade)
            }
            3 -> { // South (+Z)
                putVertex(fx, fy, fz + 1f, u0, v1, shade)
                putVertex(fx + 1f, fy, fz + 1f, u1, v1, shade)
                putVertex(fx + 1f, fy + 1f, fz + 1f, u1, v0, shade)
                putVertex(fx, fy, fz + 1f, u0, v1, shade)
                putVertex(fx + 1f, fy + 1f, fz + 1f, u1, v0, shade)
                putVertex(fx, fy + 1f, fz + 1f, u0, v0, shade)
            }
            4 -> { // West (-X)
                putVertex(fx, fy, fz, u0, v1, shade)
                putVertex(fx, fy, fz + 1f, u1, v1, shade)
                putVertex(fx, fy + 1f, fz + 1f, u1, v0, shade)
                putVertex(fx, fy, fz, u0, v1, shade)
                putVertex(fx, fy + 1f, fz + 1f, u1, v0, shade)
                putVertex(fx, fy + 1f, fz, u0, v0, shade)
            }
            5 -> { // East (+X)
                putVertex(fx + 1f, fy, fz + 1f, u0, v1, shade)
                putVertex(fx + 1f, fy, fz, u1, v1, shade)
                putVertex(fx + 1f, fy + 1f, fz, u1, v0, shade)
                putVertex(fx + 1f, fy, fz + 1f, u0, v1, shade)
                putVertex(fx + 1f, fy + 1f, fz, u1, v0, shade)
                putVertex(fx + 1f, fy + 1f, fz + 1f, u0, v0, shade)
            }
        }
    }

    private fun putVertex(x: Float, y: Float, z: Float, u: Float, v: Float, shade: Float) {
        tempVertexList.add(x)
        tempVertexList.add(y)
        tempVertexList.add(z)
        tempVertexList.add(u)
        tempVertexList.add(v)
        tempVertexList.add(shade)
    }

    private fun buildHandMesh() {
        val list = ArrayList<Float>()
        fun addBoxFace(face: Int, u0: Float, v0: Float, u1: Float, v1: Float, shade: Float) {
            val fx = -0.5f
            val fy = -0.5f
            val fz = -0.5f
            when (face) {
                0 -> { // Top
                    list.addAll(listOf(fx, fy + 1f, fz + 1f, u0, v1, shade, fx + 1f, fy + 1f, fz + 1f, u1, v1, shade, fx + 1f, fy + 1f, fz, u1, v0, shade, fx, fy + 1f, fz + 1f, u0, v1, shade, fx + 1f, fy + 1f, fz, u1, v0, shade, fx, fy + 1f, fz, u0, v0, shade))
                }
                1 -> { // Bottom
                    list.addAll(listOf(fx, fy, fz, u0, v0, shade, fx + 1f, fy, fz, u1, v0, shade, fx + 1f, fy, fz + 1f, u1, v1, shade, fx, fy, fz, u0, v0, shade, fx + 1f, fy, fz + 1f, u1, v1, shade, fx, fy, fz + 1f, u0, v1, shade))
                }
                2 -> { // North
                    list.addAll(listOf(fx + 1f, fy, fz, u0, v1, shade, fx, fy, fz, u1, v1, shade, fx, fy + 1f, fz, u1, v0, shade, fx + 1f, fy, fz, u0, v1, shade, fx, fy + 1f, fz, u1, v0, shade, fx + 1f, fy + 1f, fz, u0, v0, shade))
                }
                3 -> { // South
                    list.addAll(listOf(fx, fy, fz + 1f, u0, v1, shade, fx + 1f, fy, fz + 1f, u1, v1, shade, fx + 1f, fy + 1f, fz + 1f, u1, v0, shade, fx, fy, fz + 1f, u0, v1, shade, fx + 1f, fy + 1f, fz + 1f, u1, v0, shade, fx, fy + 1f, fz + 1f, u0, v0, shade))
                }
                4 -> { // West
                    list.addAll(listOf(fx, fy, fz, u0, v1, shade, fx, fy, fz + 1f, u1, v1, shade, fx, fy + 1f, fz + 1f, u1, v0, shade, fx, fy, fz, u0, v1, shade, fx, fy + 1f, fz + 1f, u1, v0, shade, fx, fy + 1f, fz, u0, v0, shade))
                }
                5 -> { // East
                    list.addAll(listOf(fx + 1f, fy, fz + 1f, u0, v1, shade, fx + 1f, fy, fz, u1, v1, shade, fx + 1f, fy + 1f, fz, u1, v0, shade, fx + 1f, fy, fz + 1f, u0, v1, shade, fx + 1f, fy + 1f, fz, u1, v0, shade, fx + 1f, fy + 1f, fz + 1f, u0, v0, shade))
                }
            }
        }
        val uv = TextureAtlas.getUV(2) // dirt / neutral texture
        for (f in 0..5) {
            addBoxFace(f, uv[0], uv[1], uv[2], uv[3], 0.9f)
        }
        val bb = ByteBuffer.allocateDirect(list.size * 4).order(ByteOrder.nativeOrder())
        val fb = bb.asFloatBuffer()
        val arr = FloatArray(list.size)
        for (i in list.indices) arr[i] = list[i]
        fb.put(arr)
        fb.position(0)
        handVertexBuffer = fb
        handVertexCount = list.size / 6
    }

    private fun initSkyBuffers() {
        val s = 12f
        val dist = 50f
        val sunVerts = floatArrayOf(
            -s, dist, -s, 0f, 0f, 1f,
             s, dist, -s, 1f, 0f, 1f,
             s, dist,  s, 1f, 1f, 1f,
            -s, dist, -s, 0f, 0f, 1f,
             s, dist,  s, 1f, 1f, 1f,
            -s, dist,  s, 0f, 1f, 1f
        )
        val bb = ByteBuffer.allocateDirect(sunVerts.size * 4).order(ByteOrder.nativeOrder())
        val fb = bb.asFloatBuffer()
        fb.put(sunVerts)
        fb.position(0)
        sunMoonVertexBuffer = fb
    }

    private fun initBoxLineBuffer() {
        val o = -0.002f
        val s = 1.004f
        val boxVerts = floatArrayOf(
            // Bottom square
            o, o, o,  o + s, o, o,
            o + s, o, o,  o + s, o, o + s,
            o + s, o, o + s,  o, o, o + s,
            o, o, o + s,  o, o, o,
            // Top square
            o, o + s, o,  o + s, o + s, o,
            o + s, o + s, o,  o + s, o + s, o + s,
            o + s, o + s, o + s,  o, o + s, o + s,
            o, o + s, o + s,  o, o + s, o,
            // Pillars
            o, o, o,  o, o + s, o,
            o + s, o, o,  o + s, o + s, o,
            o + s, o, o + s,  o + s, o + s, o + s,
            o, o, o + s,  o, o + s, o + s
        )
        val bb = ByteBuffer.allocateDirect(boxVerts.size * 4).order(ByteOrder.nativeOrder())
        val fb = bb.asFloatBuffer()
        fb.put(boxVerts)
        fb.position(0)
        boxLineBuffer = fb
    }

    private fun initShaders() {
        val vertexShaderCode = """
            uniform mat4 u_MVPMatrix;
            attribute vec4 a_Position;
            attribute vec2 a_TexCoordinate;
            attribute float a_Shade;
            varying vec2 v_TexCoordinate;
            varying float v_Shade;
            varying float v_Distance;
            void main() {
                v_TexCoordinate = a_TexCoordinate;
                v_Shade = a_Shade;
                gl_Position = u_MVPMatrix * a_Position;
                v_Distance = gl_Position.z;
            }
        """.trimIndent()

        val fragmentShaderCode = """
            precision mediump float;
            uniform sampler2D u_Texture;
            uniform vec4 u_ColorTint;
            uniform float u_AmbientLight;
            varying vec2 v_TexCoordinate;
            varying float v_Shade;
            varying float v_Distance;
            void main() {
                vec4 texColor = texture2D(u_Texture, v_TexCoordinate);
                if (texColor.a < 0.1) {
                    discard;
                }
                float light = v_Shade * u_AmbientLight;
                vec3 finalRgb = texColor.rgb * light * u_ColorTint.rgb;
                // Subtle fog
                float fogFactor = clamp((v_Distance - 28.0) / (55.0 - 28.0), 0.0, 1.0);
                vec3 skyFog = vec3(0.5, 0.7, 0.95) * u_AmbientLight;
                gl_FragColor = vec4(mix(finalRgb, skyFog, fogFactor * 0.75), texColor.a * u_ColorTint.a);
            }
        """.trimIndent()

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        programId = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }

        aPositionLoc = GLES20.glGetAttribLocation(programId, "a_Position")
        aTexCoordLoc = GLES20.glGetAttribLocation(programId, "a_TexCoordinate")
        aShadeLoc = GLES20.glGetAttribLocation(programId, "a_Shade")
        uMVPMatrixLoc = GLES20.glGetUniformLocation(programId, "u_MVPMatrix")
        uTextureLoc = GLES20.glGetUniformLocation(programId, "u_Texture")
        uColorTintLoc = GLES20.glGetUniformLocation(programId, "u_ColorTint")
        uAmbientLoc = GLES20.glGetUniformLocation(programId, "u_AmbientLight")
        uCameraPosLoc = GLES20.glGetUniformLocation(programId, "u_CameraPos")
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }
}
