package com.mgtriffid.games.panna.shared.game.effects.visual

import com.mgtriffid.games.cotta.EffectData
import com.mgtriffid.games.cotta.core.effects.CottaEffect

interface RailgunVisualEffect : CottaEffect {
    @EffectData
    val x1: Float

    @EffectData
    val y1: Float

    @EffectData
    val x2: Float

    @EffectData
    val y2: Float

    companion object {
        fun create(
            x1: Float, y1: Float,
            x2: Float, y2: Float
        ): RailgunVisualEffect = RailgunVisualEffectImpl(x1, y1, x2, y2)
    }
}

data class RailgunVisualEffectImpl(
    override val x1: Float,
    override val y1: Float,
    override val x2: Float,
    override val y2: Float
) : RailgunVisualEffect
