package com.mgtriffid.games.cotta.server.workload.effects

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.id.EntityId

// effects are immutable, so they will be serialized as is
data class HealthRegenerationTestEffect(val entityId: EntityId, val health: Int): CottaEffect
