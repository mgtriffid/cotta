package com.mgtriffid.games.panna.shared.game.effects.shooting

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.id.EntityId

interface RailgunShotEffect : CottaEffect {
    val shooterId: EntityId
    val shooterPlayerId: PlayerId
    val x1: Float
    val y1: Float
    val x2: Float
    val y2: Float
}
