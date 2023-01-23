package com.mgtriffid.games.cotta.server.workload.effects

import com.mgtriffid.games.cotta.core.effects.CottaEffect

// effects are immutable, so they will be serialized as is
data class HealthRegenerationTestEffect(val entityId: Int, val health: Int): CottaEffect
