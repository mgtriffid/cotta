package com.mgtriffid.games.panna.screens.game.graphics

import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.id.EntityId

interface DrawStrategy {
    fun getTexture(e: Entity): TextureId
}