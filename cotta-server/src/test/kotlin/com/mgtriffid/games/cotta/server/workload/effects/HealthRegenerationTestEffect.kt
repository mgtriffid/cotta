package com.mgtriffid.games.cotta.server.workload.effects

import com.mgtriffid.games.cotta.core.effects.CottaEffect

data class HealthRegenerationTestEffect(val entityId: Int, val health: Int): CottaEffect
