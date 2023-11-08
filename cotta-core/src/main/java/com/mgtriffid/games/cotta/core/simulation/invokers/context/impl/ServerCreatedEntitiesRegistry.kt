package com.mgtriffid.games.cotta.core.simulation.invokers.context.impl

import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.simulation.invokers.context.CreateEntityTrace

class ServerCreatedEntitiesRegistry {
    var data: Map<CreateEntityTrace, EntityId> = emptyMap()
    operator fun get(createEntityTrace: CreateEntityTrace): EntityId {
        return data[createEntityTrace] ?: throw IllegalStateException("No entity created for $createEntityTrace")
    }
}
