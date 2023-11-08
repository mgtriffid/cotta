package com.mgtriffid.games.cotta.core.simulation.invokers.context

import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.EntityId

interface CreatedEntities {
    fun record(createEntityTrace: CreateEntityTrace, entityId: EntityId)
    fun forTick(tick: Long): Map<CreateEntityTrace, EntityId>
}

interface CreateEntityTrace
data class SimplestCreateEntityTrace(val ownedBy: Entity.OwnedBy) : CreateEntityTrace
