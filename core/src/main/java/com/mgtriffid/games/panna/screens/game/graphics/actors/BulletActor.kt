package com.mgtriffid.games.panna.screens.game.graphics.actors

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Actor
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.panna.shared.game.components.physics.VelocityComponent

class BulletActor(private val textureRegion: TextureRegion) : PannaActor {
    private var angle: Float = 0f

    override val actor = object : Actor() {
        override fun draw(batch: Batch, parentAlpha: Float) {
            super.draw(batch, parentAlpha)
            batch.draw(
                /* region = */ textureRegion,
                /* x = */ x - textureRegion.regionWidth / 2,
                /* y = */ y - textureRegion.regionHeight / 2,
                /* originX = */ textureRegion.regionWidth / 2f,
                /* originY = */ textureRegion.regionHeight / 2f,
                /* width = */ textureRegion.regionWidth.toFloat(),
                /* height = */ textureRegion.regionHeight.toFloat(),
                /* scaleX = */ 1f,
                /* scaleY = */ 1f,
                /* rotation = */ angle
            )
        }
    }

    override fun update(entity: Entity) {
        val velocityComponent = entity.getComponent(VelocityComponent::class)
        angle = MathUtils.atan2Deg360(velocityComponent.velY, velocityComponent.velX)
    }
}
