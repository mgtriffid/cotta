package com.mgtriffid.games.cotta.client.invokers.impl

import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.tracing.CottaTrace

interface PredictedCreatedEntitiesRegistry {
    fun record(trace: CottaTrace, tick: Long, entityId: EntityId)

    // TODO maybe tick should become a part of trace?
    fun find(trace: CottaTrace, tick: Long): EntityId?
    fun find(tick: Long): List<Pair<CottaTrace, EntityId>>
}
