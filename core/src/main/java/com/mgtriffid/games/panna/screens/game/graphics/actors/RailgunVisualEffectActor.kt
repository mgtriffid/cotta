package com.mgtriffid.games.panna.screens.game.graphics.actors

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import space.earlygrey.shapedrawer.ShapeDrawer


class RailgunVisualEffectActor(
    private val x1: Float, private val y1: Float, private val x2: Float, private val y2: Float
) : Actor() {
    init {
        if (textureRegion == null) {
            val pm = Pixmap(1, 1, Pixmap.Format.RGBA8888)
            pm.setColor(0f, 0f, 0f, 1f)
            pm.fill()
            val tx = Texture(pm)
            textureRegion = TextureRegion(tx)
        }
    }
    companion object {
        var textureRegion: TextureRegion? = null
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)
        val drawer = ShapeDrawer(batch, textureRegion)
        drawer.line(
            x1, y1, x2, y2,
            Color(0f, 0.7f, 1f, 1f),
            Color(0f, 1f, 0.7f, 1f),
        )
    }
}

