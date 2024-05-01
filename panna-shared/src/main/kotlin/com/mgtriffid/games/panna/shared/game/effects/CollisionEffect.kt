package com.mgtriffid.games.panna.shared.game.effects

import com.mgtriffid.games.cotta.core.annotations.EffectData
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.id.EntityId

interface CollisionEffect : CottaEffect {
    @EffectData
    val id: EntityId

    @EffectData
    val id2: EntityId

    @EffectData
    val x: Float

    @EffectData
    val y: Float
}
