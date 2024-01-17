package com.mgtriffid.games.panna.screens.game.graphics.textures

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.mgtriffid.games.panna.flipped

class LeftRightRegions(val right: TextureRegion) {
    val left = right.flipped()
}
