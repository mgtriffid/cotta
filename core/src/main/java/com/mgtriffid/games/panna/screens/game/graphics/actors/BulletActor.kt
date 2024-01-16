package com.mgtriffid.games.panna.screens.game.graphics.actors

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.panna.flipped
import com.mgtriffid.games.panna.shared.game.components.physics.VelocityComponent

class BulletActor(private val textureRegion: TextureRegion) : PannaActor() {
    private val leftVersion = textureRegion.flipped() // TODO don't create a new region for every new actor

    private var orientedRight = true

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)
        batch.draw(
            if (orientedRight) textureRegion else leftVersion,
            x - textureRegion.regionWidth / 2,
            y - textureRegion.regionHeight / 2,
        )
    }

    override fun update(entity: Entity) {
        val velocityComponent = entity.getComponent(VelocityComponent::class)
        orientedRight = velocityComponent.velX > 0
    }
}
