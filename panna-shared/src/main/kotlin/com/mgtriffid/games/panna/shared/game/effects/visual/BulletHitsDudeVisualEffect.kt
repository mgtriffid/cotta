package com.mgtriffid.games.panna.shared.game.effects.visual

import com.mgtriffid.games.cotta.EffectData
import com.mgtriffid.games.cotta.core.effects.CottaEffect

interface BulletHitsDudeVisualEffect : CottaEffect {

    @EffectData
    val x: Float
    @EffectData val y: Float

    companion object {
        fun create(x: Float, y: Float): BulletHitsDudeVisualEffect = BulletHitsDudeVisualEffectImpl(x, y)
    }
}

data class BulletHitsDudeVisualEffectImpl(
    override val x: Float,
    override val y: Float
) : BulletHitsDudeVisualEffect
