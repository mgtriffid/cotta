package com.mgtriffid.games.panna.shared.game.effects.walking

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.id.EntityId

interface WalkingEffect : CottaEffect {
    val entityId: EntityId
    val direction: Byte
}
