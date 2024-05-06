package com.mgtriffid.games.panna.shared.game.effects

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.id.EntityId

interface MovementEffect : CottaEffect {
    val velocityX: Float
    val velocityY: Float
    val entityId: EntityId // should be careful when transferring this over the wire
}
