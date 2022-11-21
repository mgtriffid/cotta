package com.mgtriffid.games.panna.devutils.animationplayer.gdxadapter

import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.ScreenUtils
import com.mgtriffid.games.panna.graphics.animation.Animation
import com.mgtriffid.games.panna.graphics.animation.FrameConfig

class AnimationPlayerScreen : ScreenAdapter() {
    lateinit var animation: Animation
    lateinit var batch: SpriteBatch

    override fun show() {
        batch = SpriteBatch()
        createAnimation()
        animation.started = now()
    }

    override fun render(delta: Float) {
        ScreenUtils.clear(0.7f, 0.7f, 0.7f, 1f)
        batch.begin()
        batch.draw(animation.frameAt(now()), 200f, 200f)
        batch.end()
        // take animation
        // play animation on certain position and at certain point in time?
    }

    private fun createAnimation() {
        val textureSheet = Texture(
            "characters-free-sprites/Woodcutter/Woodcutter_idle.png"
        )
        val regions = TextureRegion.split(
            textureSheet, 48, 48
        )
        val frameRegions = regions.flatMap { it.toList() }
        animation = Animation(frameRegions.map { FrameConfig(250, it) })
    }

    private fun now() = System.currentTimeMillis()
}
