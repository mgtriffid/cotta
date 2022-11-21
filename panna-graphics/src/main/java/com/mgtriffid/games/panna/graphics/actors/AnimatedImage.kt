package com.mgtriffid.games.panna.graphics.actors

import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.mgtriffid.games.panna.graphics.animation.Animation

class AnimatedImage(val animation: Animation) : Image(animation.frameAt(animation.started)) {
    override fun act(delta: Float) {
        super.act(delta)
        (drawable as? TextureRegionDrawable)?.region = animation.frameAt(System.currentTimeMillis())
    }
}
