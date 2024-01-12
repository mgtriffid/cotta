package com.mgtriffid.games.panna.screens.game.graphics.strategies

import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.panna.screens.game.graphics.DrawStrategy
import com.mgtriffid.games.panna.screens.game.graphics.PannaTextureIds
import com.mgtriffid.games.panna.screens.game.graphics.TextureId

object SolidTerrainTileStrategy : DrawStrategy {
    override fun getTexture(e: Entity): TextureId {
        return PannaTextureIds.Terrain.TEXTURE_ID_BROWN_BLOCK
    }
}