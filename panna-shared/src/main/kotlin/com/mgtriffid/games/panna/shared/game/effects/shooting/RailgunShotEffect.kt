package com.mgtriffid.games.panna.shared.game.effects.shooting

import com.mgtriffid.games.cotta.EffectData
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.id.EntityId

interface RailgunShotEffect : CottaEffect {
    @EffectData
    val shooterId: EntityId
    @EffectData
    val shooterPlayerId: PlayerId
    @EffectData
    val x1: Float
    @EffectData
    val y1: Float
    @EffectData
    val x2: Float
    @EffectData
    val y2: Float
}
