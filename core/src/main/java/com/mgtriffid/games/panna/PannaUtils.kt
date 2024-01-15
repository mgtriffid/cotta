package com.mgtriffid.games.panna

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch

fun Batch.draw(
    texture: Texture?,
    x: Float, y: Float, originX: Float, originY: Float, width: Float, height: Float, scaleX: Float,
    scaleY: Float, rotation: Float, srcX: Int, srcY: Int, srcWidth: Int, srcHeight: Int, flipX: Boolean, flipY: Boolean
) {
    draw(
        texture, x, y, originX, originY, width, height, scaleX, scaleY, rotation, srcX, srcY, srcWidth, srcHeight,
        flipX, flipY
    )
}
