package com.mgtriffid.games.panna.shared.game.effects.visual

import com.mgtriffid.games.cotta.EffectData
import com.mgtriffid.games.cotta.core.effects.CottaEffect

interface BulletHitsGroundVisualEffect : CottaEffect {
    @EffectData val x: Float
    @EffectData val y: Float

    companion object {
        fun create(x: Float, y: Float): BulletHitsGroundVisualEffect = BulletHitsGroundVisualEffectImpl(x, y)
    }
}

data class BulletHitsGroundVisualEffectImpl(
    override val x: Float,
    override val y: Float
) : BulletHitsGroundVisualEffect
