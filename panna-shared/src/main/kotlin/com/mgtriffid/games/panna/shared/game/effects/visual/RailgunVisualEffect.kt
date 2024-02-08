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
}
