package com.example.game.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.game.gl.TextureAtlas
import com.example.game.world.ItemType

/**
 * Renders an authentic Minecraft pixel-art item texture without any text label.
 */
@Composable
fun ItemIcon(
    item: ItemType,
    modifier: Modifier = Modifier,
    size: Dp = 26.dp
) {
    val imgBitmap = remember(item) {
        TextureAtlas.getTileImageBitmap(item.iconTextureIdx)
    }
    Image(
        bitmap = imgBitmap,
        contentDescription = item.displayName,
        filterQuality = FilterQuality.None,
        modifier = modifier.size(size)
    )
}
