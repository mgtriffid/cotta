package com.mgtriffid.games.panna.screens.game.graphics.actors

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.panna.shared.game.components.physics.VelocityComponent

class BulletActor(private val textureRegion: TextureRegion) : PannaActor() {
    private var orientedRight = true
    private var angle: Float = 0f

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)
        batch.draw(
            textureRegion,
            x - textureRegion.regionWidth / 2,
            y - textureRegion.regionHeight / 2,
            textureRegion.regionWidth / 2f,
            textureRegion.regionHeight / 2f,
            textureRegion.regionWidth.toFloat(),
            textureRegion.regionHeight.toFloat(),
            1f,
            1f,
            angle
        )
    }

    override fun update(entity: Entity) {
        val velocityComponent = entity.getComponent(VelocityComponent::class)
        angle = MathUtils.atan2Deg360(velocityComponent.velY, velocityComponent.velX)
        orientedRight = velocityComponent.velX > 0
    }
}
