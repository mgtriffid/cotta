package com.mgtriffid.games.panna.shared.game.effects.visual

import com.mgtriffid.games.cotta.EffectData
import com.mgtriffid.games.cotta.core.effects.CottaEffect

interface BulletHitsGroundVisualEffect : CottaEffect {
    @EffectData val x: Float
    @EffectData val y: Float

}
