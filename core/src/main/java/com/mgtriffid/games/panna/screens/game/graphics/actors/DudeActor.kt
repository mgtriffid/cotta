package com.mgtriffid.games.panna.screens.game.graphics.actors

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.panna.flipped
import com.mgtriffid.games.panna.shared.game.components.JumpingComponent
import com.mgtriffid.games.panna.shared.game.components.LookingAtComponent

class DudeActor(
    private val regions: ActorFactory.Regions.DudeRegions,
) : PannaActor() {
    private var feetRegion: TextureRegion = regions.feetOnGround.right
    private var eyesRegion: TextureRegion = regions.eyes.lookingStraight.right

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)
        batch.draw(
            regions.body,
            x - regions.body.regionWidth / 2,
            y - regions.body.regionHeight / 2
        )
        batch.draw(feetRegion, x - feetRegion.regionWidth / 2, y - feetRegion.regionHeight / 2)
        batch.draw(eyesRegion, x - eyesRegion.regionWidth / 2, y - eyesRegion.regionHeight / 2)
    }

    override fun update(entity: Entity) {
        val feet = if (entity.getComponent(JumpingComponent::class).inAir) {
            regions.feetInAir
        } else {
            regions.feetOnGround
        }

        val lookAt = entity.getComponent(LookingAtComponent::class).lookAt
        val eyes = when (lookAt) {
            in 0f..30f -> regions.eyes.lookingStraight
            in 30f..150f -> regions.eyes.lookingUp
            in 150f..210f -> regions.eyes.lookingStraight
            in 210f..330f -> regions.eyes.lookingDown
            in 330f..360f -> regions.eyes.lookingDown
            else -> throw IllegalStateException("Look at angle $lookAt is not in range 0..360")
        }
        if (lookAt in 90f..270f) {
            feetRegion = feet.left
            eyesRegion = eyes.left
        } else {
            feetRegion = feet.right
            eyesRegion = eyes.right
        }
    }
}
