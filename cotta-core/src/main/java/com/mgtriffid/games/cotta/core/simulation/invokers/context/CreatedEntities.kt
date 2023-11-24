package com.mgtriffid.games.cotta.core.simulation.invokers.context

import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.tracing.CottaTrace

interface CreatedEntities {
    fun record(trace: CottaTrace, entityId: EntityId)
    fun forTick(tick: Long): List<Pair<CottaTrace, EntityId>>
}
