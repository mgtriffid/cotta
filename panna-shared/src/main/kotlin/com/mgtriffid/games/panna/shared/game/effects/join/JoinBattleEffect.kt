package com.mgtriffid.games.panna.shared.game.effects.join

import com.mgtriffid.games.cotta.EffectData
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.id.EntityId

interface JoinBattleEffect : CottaEffect {
    @EffectData
    val metaEntityId: EntityId
}
