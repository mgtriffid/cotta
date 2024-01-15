package com.mgtriffid.games.panna.screens.game.graphics.actors

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.mgtriffid.games.cotta.core.entities.Entity

class SolidTerrainTileActor(private val terrainTextureRegion: TextureRegion) : PannaActor() {
    override fun update(entity: Entity) {

    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)
        batch.draw(terrainTextureRegion, x - terrainTextureRegion.regionWidth / 2, y - terrainTextureRegion.regionHeight / 2)
    }
}