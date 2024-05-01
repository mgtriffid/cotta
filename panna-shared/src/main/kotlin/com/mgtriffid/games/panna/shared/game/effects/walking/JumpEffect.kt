package com.mgtriffid.games.panna.shared.game.effects.walking

import com.mgtriffid.games.cotta.core.annotations.EffectData
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.id.EntityId

interface JumpEffect : CottaEffect {
    @EffectData
    val entityId: EntityId
}
