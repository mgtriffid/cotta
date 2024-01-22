package com.mgtriffid.games.panna.screens.game.graphics

import com.mgtriffid.games.cotta.core.entities.Entity

interface DrawStrategy {
    fun getTexture(e: Entity): TextureId
}
