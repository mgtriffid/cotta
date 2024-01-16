package com.mgtriffid.games.panna

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion

fun TextureRegion.flipped(): TextureRegion {
    return TextureRegion(
        texture,
        regionX,
        regionY,
        regionWidth,
        regionHeight
    ).also { it.flip(true, false) }
}
