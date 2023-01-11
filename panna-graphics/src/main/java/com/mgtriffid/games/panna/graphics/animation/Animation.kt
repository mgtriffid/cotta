package com.mgtriffid.games.panna.graphics.animation

import com.badlogic.gdx.graphics.g2d.TextureRegion
import java.lang.IllegalStateException

class Animation(
    private val frames: List<FrameConfig>
) {
    var started = 0L
    private val animationLength by lazy { frames.sumOf { it.durationMillis  } }

    fun frameAt(time: Long): TextureRegion {
        val withinAnimation = (time - started) % animationLength
        var acc = 0
        for (frame in frames) {
            acc += frame.durationMillis
            if (acc >= withinAnimation) return frame.texture
        }
        throw IllegalStateException("Could not determine frame")
    }
}
