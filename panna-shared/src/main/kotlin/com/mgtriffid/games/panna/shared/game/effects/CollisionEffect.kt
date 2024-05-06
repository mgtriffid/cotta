package com.mgtriffid.games.panna.shared.game.effects

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.id.EntityId

interface CollisionEffect : CottaEffect {
    val id: EntityId
    val id2: EntityId
    val x: Float
    val y: Float
}
