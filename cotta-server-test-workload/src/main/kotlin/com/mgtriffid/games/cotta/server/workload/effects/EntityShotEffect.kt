package com.mgtriffid.games.cotta.server.workload.effects

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.id.EntityId

interface EntityShotEffect : CottaEffect {
    val entityId: EntityId
}
