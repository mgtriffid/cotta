package com.mgtriffid.games.panna.screens.game.graphics.actors

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.panna.flipped
import com.mgtriffid.games.panna.shared.game.components.JumpingComponent
import com.mgtriffid.games.panna.shared.game.components.LookingAtComponent
import javax.xml.soap.Text

class DudeActor(
    private val onFeetRegion: TextureRegion,
    private val inAirRegion: TextureRegion,
    private val eyesLookingUpRegion: TextureRegion,
    private val eyesLookingStraightRegion: TextureRegion,
    private val eyesLookingDownRegion: TextureRegion,
) : PannaActor() {

    private var textureRegion: TextureRegion = onFeetRegion
    private var eyesRegion: TextureRegion = eyesLookingStraightRegion

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)
        batch.draw(
            textureRegion,
            x - textureRegion.regionWidth / 2,
            y - textureRegion.regionHeight / 2
        )
        batch.draw(eyesRegion, x - eyesRegion.regionWidth / 2, y - eyesRegion.regionHeight / 2)
    }

    override fun update(entity: Entity) {
        if (entity.getComponent(JumpingComponent::class).inAir) {
            this.textureRegion = inAirRegion
        } else {
            this.textureRegion = onFeetRegion
        }

        val lookAt = entity.getComponent(LookingAtComponent::class).lookAt
        when (lookAt) {
            in 0f..30f -> eyesRegion = eyesLookingStraightRegion
            in 30f..150f -> eyesRegion = eyesLookingUpRegion
            in 150f..210f -> eyesRegion = eyesLookingStraightRegion
            in 210f..330f -> eyesRegion = eyesLookingDownRegion
            in 330f..360f -> eyesRegion = eyesLookingDownRegion
        }
        if (lookAt in 90f..270f) {
            this.textureRegion = this.textureRegion.flipped() // TODO wild memory waste, need to fix
            this.eyesRegion = this.eyesRegion.flipped()
        }
    }
}
