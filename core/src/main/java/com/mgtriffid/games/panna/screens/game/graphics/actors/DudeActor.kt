package com.mgtriffid.games.panna.screens.game.graphics.actors

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.panna.shared.game.components.JumpingComponent

class DudeActor(
    private val onFeetRegion: TextureRegion,
    private val inAirRegion: TextureRegion
) : PannaActor() {

    private var textureRegion: TextureRegion = onFeetRegion

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)
        batch.draw(textureRegion, x - textureRegion.regionWidth / 2, y - textureRegion.regionHeight / 2)
    }

    override fun update(entity: Entity) {
        if (entity.getComponent(JumpingComponent::class).inAir) {
            this.textureRegion = inAirRegion
        } else {
            this.textureRegion = onFeetRegion
        }
    }
}