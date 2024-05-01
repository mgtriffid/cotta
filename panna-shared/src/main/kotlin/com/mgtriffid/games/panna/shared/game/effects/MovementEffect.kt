package com.mgtriffid.games.panna.shared.game.effects

import com.mgtriffid.games.cotta.core.annotations.EffectData
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.id.EntityId

interface MovementEffect : CottaEffect {
    @EffectData
    val velocityX: Float
    @EffectData
    val velocityY: Float
    @EffectData
    val entityId: EntityId // should be careful when transferring this over the wire
}
