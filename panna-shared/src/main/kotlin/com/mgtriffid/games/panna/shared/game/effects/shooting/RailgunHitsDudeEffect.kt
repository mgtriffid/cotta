package com.mgtriffid.games.panna.shared.game.effects.shooting

import com.mgtriffid.games.cotta.core.annotations.EffectData
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.id.EntityId

interface RailgunHitsDudeEffect : CottaEffect {
    @EffectData
    val shotId: EntityId
}
