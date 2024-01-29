package com.mgtriffid.games.panna.screens.game.graphics.actors

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.mgtriffid.games.cotta.utils.now
import kotlin.math.min

class BulletHitsGroundVisualEffectActor(val bulletHitsGround: List<TextureRegion>) : Actor() {
    private val createdAt = now()

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)
        val region = chooseAnimationFrame()
        batch.draw(region, x - region.regionWidth / 2, y - region.regionHeight / 2)
    }

    private fun chooseAnimationFrame() = bulletHitsGround[min((now() - createdAt) / 100L, 3L).toInt()]
}
