package com.mgtriffid.games.panna.screens.game.graphics.actors

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.mgtriffid.games.cotta.core.entities.Entity

class BulletActor(val textureRegion: TextureRegion) : PannaActor() {
    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)
        batch.draw(textureRegion, x - textureRegion.regionWidth / 2, y - textureRegion.regionHeight / 2)
    }

    override fun update(entity: Entity) {

    }
}
