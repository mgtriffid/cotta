package com.mgtriffid.games.panna.shared.game.effects

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.EntityId

data class MovementEffect(
    val direction: Byte,
    val velocity: Int,
    val entityId: EntityId // should be careful when transferring this over the wire
): CottaEffect
